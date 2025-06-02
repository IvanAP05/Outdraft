import logging
import time
from datetime import datetime
from typing import Dict, List, Optional, Set

import requests
from supabase import create_client, Client

# Configurar logging (con encoding UTF-8 para Windows)
import sys

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('matchup_analysis.log', encoding='utf-8'),
        logging.StreamHandler(sys.stdout)
    ]
)

# Configurar encoding para Windows
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
logger = logging.getLogger(__name__)

# ================================
# CONFIGURACIÓN
# ================================
RIOT_API_KEY = "RGAPI-b2883465-e745-4c1e-a30c-a15de3a49c76"  # ⚠️ IMPORTANTE: Cambia por tu API key real

# CONFIGURACIÓN DE SUPABASE
SUPABASE_URL = "https://egrizauyajhrzxxzibpd.supabase.co"  # ej: https://xyzcompany.supabase.co
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncml6YXV5YWpocnp4eHppYnBkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3MDg4MzcsImV4cCI6MjA2NDI4NDgzN30.8NU291tbHg2q87HCZ3eDuPQn9GRNLzhVoEyVYFdiS28"  # Tu anon/public key

REGION = "euw1"  # Cambia según tu región (euw1, na1, kr, etc.)

# CONFIGURACIÓN PRINCIPAL - Ahora analiza TODOS los rangos automáticamente
MAX_PLAYERS_PER_RANK = 100  # Número de jugadores a analizar por cada rango
MATCHES_PER_PLAYER = 20  # Partidas por jugador (más partidas = más datos)

# Timestamp del parche actual (14.24) - ajustar según el parche que quieras analizar
CURRENT_PATCH_START = 1734566400  # 19 de diciembre 2024 (ajustar según el parche real)

RATE_LIMIT_DELAY = 2.0  # Segundos entre requests (aumentado para evitar rate limit)

# Definir todos los rangos de League of Legends en orden ascendente

RANKS_DONE = [
    ("IRON", "IV"), ("IRON", "III"), ("IRON", "II"), ("IRON", "I"),
    ("BRONZE", "IV"), ("BRONZE", "III"), ("BRONZE", "II"), ("BRONZE", "I"),
]
ALL_RANKS = [

    ("SILVER", "IV"), ("SILVER", "III"), ("SILVER", "II"), ("SILVER", "I"),
    ("GOLD", "IV"), ("GOLD", "III"), ("GOLD", "II"), ("GOLD", "I"),
    ("PLATINUM", "IV"), ("PLATINUM", "III"), ("PLATINUM", "II"), ("PLATINUM", "I"),
    ("EMERALD", "IV"), ("EMERALD", "III"), ("EMERALD", "II"), ("EMERALD", "I"),
    ("DIAMOND", "IV"), ("DIAMOND", "III"), ("DIAMOND", "II"), ("DIAMOND", "I")
]


class RiotAPIClient:
    def __init__(self, api_key: str, region: str = "euw1"):
        self.api_key = api_key
        self.region = region
        self.base_url = f"https://{region}.api.riotgames.com"
        self.continental_url = "https://europe.api.riotgames.com"
        self.rate_limit_delay = RATE_LIMIT_DELAY

    def _make_request(self, url: str) -> Optional[Dict]:
        headers = {"X-Riot-Token": self.api_key}
        try:
            time.sleep(self.rate_limit_delay)
            response = requests.get(url, headers=headers)

            if response.status_code == 200:
                return response.json()
            elif response.status_code == 429:
                retry_after = int(response.headers.get("Retry-After", 60))
                logger.warning(f"Rate limit alcanzado, esperando {retry_after} segundos...")
                time.sleep(retry_after)
                return self._make_request(url)
            elif response.status_code == 404:
                logger.warning(f"Recurso no encontrado: {url}")
                return None
            else:
                logger.error(f"Error {response.status_code}: {response.text}")
                return None
        except Exception as e:
            logger.error(f"Error en request: {e}")
            return None

    def get_summoner_by_rank(self, tier: str, division: str, page: int = 1) -> Optional[List[Dict]]:
        queue = "RANKED_SOLO_5x5"
        url = f"{self.base_url}/lol/league/v4/entries/{queue}/{tier}/{division}?page={page}"
        return self._make_request(url)

    def get_summoner_by_summoner_id(self, summoner_id: str) -> Optional[Dict]:
        url = f"{self.base_url}/lol/summoner/v4/summoners/{summoner_id}"
        return self._make_request(url)

    def get_match_ids(self, puuid: str, count: int = 20, start_time: int = None) -> Optional[List[str]]:
        url = f"{self.continental_url}/lol/match/v5/matches/by-puuid/{puuid}/ids?type=ranked&count={count}"
        if start_time:
            url += f"&startTime={start_time}"
        return self._make_request(url)

    def get_match_details(self, match_id: str) -> Optional[Dict]:
        url = f"{self.continental_url}/lol/match/v5/matches/{match_id}"
        return self._make_request(url)

    def get_match_timeline(self, match_id: str) -> Optional[Dict]:
        url = f"{self.continental_url}/lol/match/v5/matches/{match_id}/timeline"
        return self._make_request(url)


class MatchupAnalyzer:
    def __init__(self, riot_client: RiotAPIClient, supabase_client: Client):
        self.riot_client = riot_client
        self.supabase = supabase_client
        # Mapeo de posiciones de la API a nombres más simples
        self.position_mapping = {
            'TOP': 'top',
            'JUNGLE': 'jungle',
            'MIDDLE': 'mid',
            'BOTTOM': 'bot',
            'UTILITY': 'support'
        }
        # Set para trackear partidas ya analizadas
        self.analyzed_matches: Set[str] = set()
        # Cargar partidas ya analizadas desde Supabase al inicializar
        self._load_analyzed_matches()

    def _get_tier_group(self, tier: str) -> str:
        """
        Convierte el tier específico al grupo principal
        Ejemplo: IRON IV -> IRON, BRONZE II -> BRONZE, etc.
        """
        return tier.upper()

    def _load_analyzed_matches(self):
        """Carga las partidas ya analizadas desde Supabase para evitar duplicados"""
        try:
            logger.info("🔄 Cargando partidas ya analizadas desde Supabase...")

            # Obtener TODOS los registros de analyzed_matches sin límite
            all_matches = []
            offset = 0
            limit = 1000  # Procesar en lotes de 1000

            while True:
                response = self.supabase.table('analyzed_matches') \
                    .select('match_id') \
                    .range(offset, offset + limit - 1) \
                    .execute()

                if not response.data:
                    break

                all_matches.extend(response.data)

                # Si obtuvimos menos registros que el límite, ya terminamos
                if len(response.data) < limit:
                    break

                offset += limit

            # Añadir todas las partidas al set
            for match in all_matches:
                if match.get('match_id'):
                    self.analyzed_matches.add(match['match_id'])

            logger.info(f"📊 Cargadas {len(self.analyzed_matches)} partidas ya analizadas desde Supabase")

            # Log de algunos ejemplos para debug
            if self.analyzed_matches:
                sample_matches = list(self.analyzed_matches)[:5]
                logger.debug(f"Ejemplos de partidas cargadas: {sample_matches}")

        except Exception as e:
            logger.error(f"❌ Error cargando partidas analizadas: {e}")
            self.analyzed_matches = set()

    def _mark_match_as_analyzed(self, match_id: str):
        """Marca una partida como analizada en Supabase y en memoria"""
        try:
            # Verificar si ya existe en memoria para evitar duplicados en Supabase
            if match_id in self.analyzed_matches:
                logger.debug(f"Partida {match_id} ya está marcada como analizada")
                return

            # Insertar en Supabase
            self.supabase.table('analyzed_matches').insert({
                'match_id': match_id,
                'analyzed_at': datetime.now().isoformat()
            }).execute()

            # Añadir a memoria
            self.analyzed_matches.add(match_id)
            logger.debug(f"✅ Partida {match_id} marcada como analizada")

        except Exception as e:
            # Si el error es por duplicado, simplemente añadir a memoria
            if "duplicate key value" in str(e).lower() or "unique constraint" in str(e).lower():
                self.analyzed_matches.add(match_id)
                logger.debug(f"Partida {match_id} ya existía en BD, añadida a memoria")
            else:
                logger.error(f"❌ Error marcando partida como analizada: {e}")

    def _is_match_analyzed(self, match_id: str) -> bool:
        """Verifica si una partida ya fue analizada"""
        is_analyzed = match_id in self.analyzed_matches
        if is_analyzed:
            logger.debug(f"🔍 Partida {match_id} ya analizada - SKIP")
        return is_analyzed

    def get_gold_advantage_at_14min(self, timeline_data: Dict, participant_id_1: int, participant_id_2: int) -> \
            Optional[int]:
        """
        Calcula la diferencia de oro entre dos jugadores en el minuto 14 (840,000 ms)
        """
        try:
            frames = timeline_data.get('info', {}).get('frames', [])
            target_time = 840000  # 14 minutos en millisegundos

            # Buscar el frame más cercano a los 14 minutos
            closest_frame = None
            for frame in frames:
                if frame.get('timestamp', 0) >= target_time:
                    closest_frame = frame
                    break

            # Si no hay frame a los 14 min, usar el último disponible
            if not closest_frame and frames:
                closest_frame = frames[-1]

            if not closest_frame:
                logger.warning("No se encontró frame para calcular oro a los 14 min")
                return None

            participant_frames = closest_frame.get('participantFrames', {})

            # Obtener oro total de cada participante
            gold_p1 = participant_frames.get(str(participant_id_1), {}).get('totalGold', 0)
            gold_p2 = participant_frames.get(str(participant_id_2), {}).get('totalGold', 0)

            # Retornar diferencia (positivo = ventaja para p1, negativo = ventaja para p2)
            return gold_p1 - gold_p2

        except Exception as e:
            logger.error(f"Error calculando ventaja de oro: {e}")
            return None

    def analyze_match(self, match_data: Dict) -> List[Dict]:
        """
        Analiza una partida y extrae los matchups 1v1 de cada línea
        """
        try:
            match_id = match_data.get('metadata', {}).get('matchId', '')

            # VERIFICACIÓN TEMPRANA: Si la partida ya fue analizada, salir inmediatamente
            if self._is_match_analyzed(match_id):
                return []

            participants = match_data.get('info', {}).get('participants', [])
            game_duration = match_data.get('info', {}).get('gameDuration', 0)
            game_creation = match_data.get('info', {}).get('gameCreation', 0) // 1000  # Convertir a segundos

            # Verificar que la partida es del parche actual
            if game_creation < CURRENT_PATCH_START:
                logger.debug(f"Partida {match_id} es de un parche anterior, omitiendo...")
                return []

            # Solo analizar partidas que duraron más de 10 minutos
            if game_duration < 600:  # 600 segundos = 10 minutos
                logger.debug(f"Partida {match_id} muy corta ({game_duration}s), omitiendo...")
                return []

            # Obtener timeline para calcular ventaja de oro
            timeline = self.riot_client.get_match_timeline(match_id)
            if not timeline:
                logger.warning(f"No se pudo obtener timeline para {match_id}")
                return []

            # Organizar participantes por equipo y posición
            team_positions = {100: {}, 200: {}}  # Team 100 (azul) y Team 200 (rojo)

            for participant in participants:
                position = participant.get('teamPosition')
                team_id = participant.get('teamId')

                if position in self.position_mapping:
                    team_positions[team_id][position] = {
                        'championId': participant['championId'],
                        'participantId': participant['participantId'],
                        'championName': participant['championName'],
                        'won': participant['win']
                    }

            matchups = []

            # Crear matchups para cada posición donde hay enfrentamiento 1v1
            for position in self.position_mapping.keys():
                if position in team_positions[100] and position in team_positions[200]:
                    p1 = team_positions[100][position]
                    p2 = team_positions[200][position]

                    # Calcular ventaja de oro a los 14 minutos
                    gold_advantage = self.get_gold_advantage_at_14min(
                        timeline,
                        p1['participantId'],
                        p2['participantId']
                    )

                    if gold_advantage is None:
                        logger.warning(f"No se pudo calcular ventaja de oro para {position} en {match_id}")
                        continue

                    # Determinar ganador basado en ventaja de oro
                    # Si la diferencia es muy pequeña (menos de 300 oro), consideramos empate y omitimos
                    if abs(gold_advantage) < 300:
                        logger.debug(f"Diferencia de oro muy pequeña en {position}: {gold_advantage}")
                        continue

                    # El ganador es quien tiene más oro a los 14 min
                    if gold_advantage > 0:
                        winner_champion = p1['championId']
                        winner_name = p1['championName']
                    else:
                        winner_champion = p2['championId']
                        winner_name = p2['championName']

                    matchup = {
                        'position': self.position_mapping[position],
                        'champion1': p1['championId'],
                        'champion1_name': p1['championName'],
                        'champion2': p2['championId'],
                        'champion2_name': p2['championName'],
                        'winner': winner_champion,
                        'winner_name': winner_name,
                        'gold_advantage': abs(gold_advantage),
                        'match_id': match_id,
                        'game_duration': game_duration,
                        'game_creation': game_creation
                    }

                    matchups.append(matchup)
                    logger.debug(
                        f"Matchup encontrado: {p1['championName']} vs {p2['championName']} ({position}) - Ganador: {winner_name}")

            # IMPORTANTE: Solo marcar como analizada si encontramos matchups válidos
            # O si la partida cumple criterios pero no tiene matchups válidos
            if matchups or (game_duration >= 600 and game_creation >= CURRENT_PATCH_START):
                self._mark_match_as_analyzed(match_id)
                logger.debug(f"🔖 Partida {match_id} marcada como analizada ({len(matchups)} matchups)")

            return matchups

        except Exception as e:
            logger.error(f"Error analizando partida {match_data.get('metadata', {}).get('matchId', 'unknown')}: {e}")
            return []

    def save_matchup_to_supabase(self, matchup: Dict, tier: str):
        """
        Guarda el matchup en Supabase, actualizando contadores para ambos campeones
        """
        try:
            # Convertir el tier específico al grupo principal
            rank_group = self._get_tier_group(tier)

            logger.debug(f"Guardando matchup para rango agrupado: {rank_group} (original: {tier})")

            # Guardar desde perspectiva del campeón 1
            self._save_single_matchup(
                rank=rank_group,
                position=matchup['position'],
                champion_id=matchup['champion1'],
                champion_name=matchup['champion1_name'],
                opponent_id=matchup['champion2'],
                opponent_name=matchup['champion2_name'],
                won=matchup['winner'] == matchup['champion1']
            )

            # Guardar desde perspectiva del campeón 2
            self._save_single_matchup(
                rank=rank_group,
                position=matchup['position'],
                champion_id=matchup['champion2'],
                champion_name=matchup['champion2_name'],
                opponent_id=matchup['champion1'],
                opponent_name=matchup['champion1_name'],
                won=matchup['winner'] == matchup['champion2']
            )

        except Exception as e:
            logger.error(f"Error guardando matchup en Supabase: {e}")

    def _save_single_matchup(self, rank: str, position: str, champion_id: int, champion_name: str,
                             opponent_id: int, opponent_name: str, won: bool):
        """
        Guarda un matchup desde la perspectiva de un campeón específico usando UPSERT
        """
        try:
            # Crear ID único para el documento
            doc_id = f"{rank}_{position}_{champion_id}_{opponent_id}"

            # Verificar si el matchup ya existe
            existing = self.supabase.table('matchups').select('*').eq('id', doc_id).execute()

            if existing.data:
                # El matchup existe, actualizar contadores
                current_data = existing.data[0]
                games_played = current_data.get('games_played', 0) + 1
                games_won = current_data.get('games_won', 0) + (1 if won else 0)

                # Calcular winrate
                winrate = (games_won / games_played) * 100 if games_played > 0 else 0

                # Actualizar registro existente
                self.supabase.table('matchups').update({
                    'games_played': games_played,
                    'games_won': games_won,
                    'winrate': round(winrate, 2),
                    'last_updated': datetime.now().isoformat()
                }).eq('id', doc_id).execute()

            else:
                # El matchup no existe, crear nuevo
                games_played = 1
                games_won = 1 if won else 0
                winrate = (games_won / games_played) * 100

                self.supabase.table('matchups').insert({
                    'id': doc_id,
                    'rank': rank,
                    'position': position,
                    'champion_id': champion_id,
                    'champion_name': champion_name,
                    'opponent_id': opponent_id,
                    'opponent_name': opponent_name,
                    'games_played': games_played,
                    'games_won': games_won,
                    'winrate': round(winrate, 2),
                    'last_updated': datetime.now().isoformat()
                }).execute()

        except Exception as e:
            logger.error(f"Error guardando matchup individual: {e}")

    def process_rank(self, tier: str, division: str, max_players: int, matches_per_player: int):
        """
        Procesa todos los jugadores de un rango específico
        """
        rank_name = f"{tier}_{division}"
        grouped_rank = self._get_tier_group(tier)

        logger.info(f"🔍 Procesando división: {rank_name} -> Agrupando en: {grouped_rank}")

        players_processed = 0
        page = 1
        total_matchups = 0
        skipped_matches = 0

        while players_processed < max_players:
            logger.info(f"Obteniendo jugadores - Página {page}")
            players = self.riot_client.get_summoner_by_rank(tier, division, page)

            if not players or len(players) == 0:
                logger.warning(f"No más jugadores disponibles en página {page}")
                break

            for player in players:
                if players_processed >= max_players:
                    break

                try:
                    summoner_name = player.get('summonerName', 'Unknown')
                    logger.info(f"Procesando jugador {players_processed + 1}/{max_players}: {summoner_name}")

                    # Obtener datos del invocador
                    summoner_data = self.riot_client.get_summoner_by_summoner_id(player['summonerId'])
                    if not summoner_data:
                        continue

                    puuid = summoner_data['puuid']

                    # Obtener IDs de partidas del parche actual
                    match_ids = self.riot_client.get_match_ids(
                        puuid,
                        matches_per_player,
                        CURRENT_PATCH_START
                    )

                    if not match_ids:
                        logger.warning(f"No se encontraron partidas del parche actual para {summoner_name}")
                        continue

                    logger.info(f"Analizando {len(match_ids)} partidas de {summoner_name}")

                    # Filtrar partidas ya analizadas ANTES de hacer requests
                    new_match_ids = [mid for mid in match_ids if not self._is_match_analyzed(mid)]
                    skipped_count = len(match_ids) - len(new_match_ids)
                    skipped_matches += skipped_count

                    if skipped_count > 0:
                        logger.info(f"⏭️ Omitiendo {skipped_count} partidas ya analizadas para {summoner_name}")

                    if not new_match_ids:
                        logger.info(f"📋 Todas las partidas de {summoner_name} ya fueron analizadas")
                        players_processed += 1
                        continue

                    logger.info(f"🆕 Analizando {len(new_match_ids)} partidas nuevas de {summoner_name}")

                    # Analizar solo las partidas nuevas
                    for i, match_id in enumerate(new_match_ids):
                        logger.debug(f"Analizando partida {i + 1}/{len(new_match_ids)}: {match_id}")

                        match_data = self.riot_client.get_match_details(match_id)
                        if not match_data:
                            continue

                        matchups = self.analyze_match(match_data)

                        # Guardar cada matchup encontrado
                        for matchup in matchups:
                            self.save_matchup_to_supabase(matchup, tier)
                            total_matchups += 1

                    players_processed += 1
                    logger.info(f"✅ Completado jugador {summoner_name} - Total matchups acumulados: {total_matchups}")

                except Exception as e:
                    logger.error(f"Error procesando jugador {player.get('summonerName', 'Unknown')}: {e}")
                    continue

            page += 1
            # Evitar bucle infinito
            if page > 20:
                logger.warning("Alcanzado límite de páginas")
                break

            time.sleep(2)  # Pequeña pausa entre páginas

        logger.info(
            f"🎉 Completada división {rank_name} -> {grouped_rank}. Jugadores: {players_processed}, Matchups: {total_matchups}, Omitidas: {skipped_matches}")
        return total_matchups

    def run_full_analysis(self):
        """
        Ejecuta el análisis para todos los rangos automáticamente
        """
        logger.info("🚀 Iniciando análisis completo de TODOS LOS RANGOS")
        logger.info("📊 NUEVA FUNCIONALIDAD: Los datos se agruparán por rango principal")
        logger.info("   • IRON IV, III, II, I → se agrupan como IRON")
        logger.info("   • BRONZE IV, III, II, I → se agrupan como BRONZE")
        logger.info("   • Y así sucesivamente...")
        logger.info(
            f"📊 Configuración: {MAX_PLAYERS_PER_RANK} jugadores por rango, {MATCHES_PER_PLAYER} partidas por jugador")
        logger.info(f"📅 Solo partidas desde: {datetime.fromtimestamp(CURRENT_PATCH_START)}")

        total_matchups_global = 0
        completed_ranks = 0

        # Diccionario para trackear matchups por rango agrupado
        matchups_by_group = {}

        for tier, division in ALL_RANKS:
            try:
                grouped_rank = self._get_tier_group(tier)

                logger.info(f"\n{'=' * 60}")
                logger.info(f"🎯 DIVISIÓN {completed_ranks + 1}/{len(ALL_RANKS)}: {tier} {division}")
                logger.info(f"📂 Se agrupará en: {grouped_rank}")
                logger.info(f"{'=' * 60}")

                matchups_for_rank = self.process_rank(tier, division, MAX_PLAYERS_PER_RANK, MATCHES_PER_PLAYER)
                total_matchups_global += matchups_for_rank
                completed_ranks += 1

                # Trackear matchups por grupo
                if grouped_rank not in matchups_by_group:
                    matchups_by_group[grouped_rank] = 0
                matchups_by_group[grouped_rank] += matchups_for_rank

                logger.info(f"✅ Completado {tier} {division}: {matchups_for_rank} matchups")
                logger.info(f"📈 Progreso global: {completed_ranks}/{len(ALL_RANKS)} divisiones completadas")
                logger.info(f"🎯 Total matchups acumulados: {total_matchups_global}")

                # Mostrar progreso por grupos
                logger.info(f"📊 Matchups por rango agrupado hasta ahora:")
                for group, count in sorted(matchups_by_group.items()):
                    logger.info(f"   • {group}: {count} matchups")

                # Pausa entre rangos para evitar rate limits
                if completed_ranks < len(ALL_RANKS):
                    logger.info("⏸️ Pausa de 10 segundos entre divisiones...")
                    time.sleep(10)

            except KeyboardInterrupt:
                logger.info(f"\n⏹️ Análisis interrumpido por el usuario en {tier} {division}")
                logger.info(f"📊 Progreso hasta interrupción: {completed_ranks}/{len(ALL_RANKS)} divisiones completadas")
                logger.info(f"🎯 Total matchups analizados: {total_matchups_global}")
                break
            except Exception as e:
                logger.error(f"❌ Error procesando {tier} {division}: {e}")
                logger.error("Continuando con la siguiente división...")
                continue

        logger.info(f"\n🏁 ANÁLISIS COMPLETO FINALIZADO")
        logger.info(f"✅ Divisiones completadas: {completed_ranks}/{len(ALL_RANKS)}")
        logger.info(f"🎯 Total de matchups analizados: {total_matchups_global}")

        logger.info(f"\n📊 RESUMEN FINAL POR RANGO AGRUPADO:")
        for group, count in sorted(matchups_by_group.items()):
            logger.info(f"   🏆 {group}: {count} matchups")

        return total_matchups_global


def initialize_supabase() -> Client:
    """Inicializa la conexión con Supabase"""
    try:
        supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
        return supabase
    except Exception as e:
        logger.error(f"Error inicializando Supabase: {e}")
        raise


def query_winrates(rank_filter: str = None):
    """
    Consulta y muestra los winrates guardados en Supabase
    """
    try:
        supabase = initialize_supabase()

        # Construir query
        query = supabase.table('matchups').select('*')
        if rank_filter:
            query = query.eq('rank', rank_filter)

        response = query.limit(20).execute()

        print(f"\n🏆 === WINRATES EN SUPABASE ({rank_filter or 'TODOS LOS RANGOS AGRUPADOS'}) ===")
        print("-" * 80)

        for matchup in response.data:
            print(
                f"🥊 {matchup['champion_name']} vs {matchup['opponent_name']} ({matchup['position'].upper()}, {matchup['rank']})")
            print(
                f"   📊 Winrate: {matchup['winrate']:.1f}% ({matchup['games_won']}/{matchup['games_played']} partidas)")
            print()

        # Contar total de matchups
        total_response = supabase.table('matchups').select('id', count='exact').execute()
        total_count = total_response.count

        print(f"📈 Total de matchups en base de datos: {total_count}")

        # Mostrar distribución por rangos agrupados
        print(f"\n📊 Distribución por rangos agrupados:")

        # Obtener todos los rangos únicos
        ranks_response = supabase.table('matchups').select('rank').execute()
        rank_distribution = {}

        for matchup in ranks_response.data:
            rank = matchup.get('rank', 'UNKNOWN')
            if rank not in rank_distribution:
                rank_distribution[rank] = 0
            rank_distribution[rank] += 1

        for rank, count in sorted(rank_distribution.items()):
            print(f"   🏆 {rank}: {count} matchups")

    except Exception as e:
        logger.error(f"Error consultando winrates: {e}")


def main():
    """Función principal"""
    logger.info("=" * 60)
    logger.info("ANALIZADOR DE MATCHUPS DE LEAGUE OF LEGENDS - SUPABASE VERSION")
    logger.info("=" * 60)

    # Verificar configuración
    if RIOT_API_KEY == "TU_API_KEY_AQUI":
        logger.error("ERROR: Debes configurar tu RIOT_API_KEY")
        logger.error("Obtén tu API key en: https://developer.riotgames.com/")
        return

    if SUPABASE_URL == "TU_SUPABASE_URL_AQUI" or SUPABASE_KEY == "TU_SUPABASE_ANON_KEY_AQUI":
        logger.error("ERROR: Debes configurar SUPABASE_URL y SUPABASE_KEY")
        logger.error("Obtén tus credenciales en: https://app.supabase.com/")
        return

    try:
        # Inicializar servicios
        logger.info("Inicializando Supabase...")
        supabase = initialize_supabase()

        logger.info("Inicializando cliente de Riot API...")
        riot_client = RiotAPIClient(RIOT_API_KEY, REGION)
        analyzer = MatchupAnalyzer(riot_client, supabase)

        # Ejecutar análisis completo de todos los rangos
        logger.info("🎮 Iniciando análisis de TODAS LAS DIVISIONES con agrupamiento por rangos")
        logger.info(f"🔄 Solo se analizarán partidas desde: {datetime.fromtimestamp(CURRENT_PATCH_START)}")
        logger.info(f"🎯 {MAX_PLAYERS_PER_RANK} jugadores por división, {MATCHES_PER_PLAYER} partidas por jugador")
        logger.info("📂 Los datos se guardarán agrupados por rango principal (IRON, BRONZE, SILVER, etc.) en Supabase")

        total_matchups = analyzer.run_full_analysis()

        if total_matchups > 0:
            logger.info("🎉 Análisis completado exitosamente!")

            # Mostrar algunos resultados
            print("\n" + "=" * 60)
            query_winrates()
        else:
            logger.warning("⚠️ No se encontraron matchups para analizar")

    except KeyboardInterrupt:
        logger.info("⏹️ Análisis interrumpido por el usuario")
    except Exception as e:
        logger.error(f"💥 Error crítico en main: {e}")
        import traceback
        logger.error(traceback.format_exc())


if __name__ == "__main__":
    main()