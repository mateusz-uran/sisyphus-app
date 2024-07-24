import json
import os
from flask import Flask, request, jsonify
from flask_cors import CORS
from scraper import scrap_nofluffjobs, scrap_pracuj

frontend_url = os.environ['FRONTEND_URL']

app = Flask(__name__)
CORS(app, origins=frontend_url)

@app.route('/scrape/nofluffjobs', methods=['POST'])
def scrape_nofluffjobs_endpoint():
    url = request.json.get('url')
    if not url:
        return jsonify({'error': 'URL is required'}), 400

    try:
        result = scrap_nofluffjobs(url)
        return jsonify(json.loads(result))
    except Exception as e:
        return jsonify({'error': f'An error occurred: {str(e)}'}), 500


@app.route('/scrape/pracuj', methods=['POST'])
def scrape_pracuj_endpoint():
    url = request.json.get('url')
    if not url:
        return jsonify({'error': 'URL is required'}), 400

    try:
        result = scrap_pracuj(url)
        return jsonify(json.loads(result))
    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5858)
