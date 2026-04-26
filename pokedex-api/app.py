import os
import requests
import psycopg2
from flask import Flask, jsonify

# Incializa Flask
app = Flask(__name__)

# Función para obtener la conexión a la base de datos desde las variables de entorno del .env
def get_db_connection():
    conn = psycopg2.connect(
        host=os.environ.get('POSTGRES_HOST', 'localhost'),
        database=os.environ.get('POSTGRES_DB', 'pokedex'),
        user=os.environ.get('POSTGRES_USER', 'pokedex_user'),
        password=os.environ.get('POSTGRES_PASSWORD', 'pokedex_password'),
        port=os.environ.get('POSTGRES_PORT', '5432')
    )
    return conn

# Endpoint para obtener la lista general de Pokémon desde la pokeapi
@app.route('/api/pokemon', methods=['GET'])
def get_pokemon_list():
    try:
        # Petición a la API externa limitando a 50 resultados, paginado
        response = requests.get('https://pokeapi.co/api/v2/pokemon?limit=50', timeout=5)
        response.raise_for_status()
        data = response.json()
        
        # Parsear la lista y añadir las URLs de las imágenes de cada Pokémon
        pokemon_list = []
        for item in data.get('results', []):
            name = item.get('name')
            url = item.get('url')
            # Extraer ID desde la URL
            poke_id = url.strip('/').split('/')[-1]
            image_url = f"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{poke_id}.png"
            pokemon_list.append({
                "id": poke_id,
                "name": name,
                "image": image_url
            })
            
        return jsonify(pokemon_list)
    except requests.exceptions.RequestException as e:
        # En caso de error de red o timeout, tira una excepción
        return jsonify({"error": "API_ERROR", "message": f"Fallo al conectar con PokeAPI: {str(e)}"}), 502

# Endpoint para buscar un Pokémon por su nombre
@app.route('/api/pokemon/<name>', methods=['GET'])
def get_pokemon(name):
    # Llama a la pokeapi
    try:
        response = requests.get(f'https://pokeapi.co/api/v2/pokemon/{name}', timeout=5)
        response.raise_for_status()
        return jsonify(response.json())
    except requests.exceptions.RequestException as e:
        # Devuelve un error 502 si salta una excepciíon
        return jsonify({"error": "API_ERROR", "message": f"Fallo al obtener datos de PokeAPI: {str(e)}"}), 502

# Endpoint para obtener los Pokémon personalizados creados en la base de datos mysql
@app.route('/api/db/pokemon', methods=['GET'])
def get_db_pokemon():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute('SELECT id, name, type, description FROM custom_pokemon')
        rows = cur.fetchall()
        pokemon_list = [{"id": r[0], "name": r[1], "type": r[2], "description": r[3]} for r in rows]
        cur.close()
        conn.close()
        return jsonify(pokemon_list)
    except psycopg2.Error as e:
        # Devuelve un error 500 en caso de fallo en la base de datos
        return jsonify({"error": "DB_ERROR", "message": f"Fallo en el acceso a la base de datos: {str(e)}"}), 500

# Rutas de Simulación de Errores

# Simular un error de archivo (Error de I/O)
@app.route('/api/test/error/file', methods=['GET'])
def simulate_file_error():
    try:
        # Intentamos leer un archivo que no existe
        with open('non_existent_file.txt', 'r') as f:
            content = f.read()
        return jsonify({"content": content})
    except FileNotFoundError as e:
        return jsonify({"error": "FILE_NOT_FOUND", "message": f"No se pudo leer el archivo: {str(e)}"}), 404
    except Exception as e:
        return jsonify({"error": "INTERNAL_ERROR", "message": str(e)}), 500

# Simular un error de base de datos
@app.route('/api/test/error/db', methods=['GET'])
def simulate_db_error():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        # Error intencionado haciendo consulta SQL a tabla inexistente
        cur.execute('SELEC * FROM non_existent_table')
        rows = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify(rows)
    except psycopg2.Error as e:
        return jsonify({"error": "DB_SYNTAX_ERROR", "message": f"Error de BD simulado: {str(e)}"}), 500
    except Exception as e:
        return jsonify({"error": "INTERNAL_ERROR", "message": str(e)}), 500

# Llamada errónea a un endpoint que no existe en la pokeapi
@app.route('/api/test/error/api', methods=['GET'])
def simulate_api_error():
    try:
        # Llamada intencionada a un endpoint que no existe
        response = requests.get('https://pokeapi.co/api/v2/non_existent_endpoint', timeout=2)
        response.raise_for_status()
        return jsonify(response.json())
    except requests.exceptions.HTTPError as e:
        return jsonify({"error": "THIRD_PARTY_API_ERROR", "message": f"Error de API simulado: {str(e)}"}), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"error": "THIRD_PARTY_NETWORK_ERROR", "message": f"Error de API simulado: {str(e)}"}), 502

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
