import os
from flask import Flask, request, jsonify
from flask_cors import CORS
from scrapers import handle_scrapers

frontend_url = os.environ.get("FRONTEND_URL", "http://localhost:4200")

app = Flask(__name__)
CORS(app, origins=frontend_url)


@app.route('/scrape', methods=['POST'])
def scrape_endpoint():
    url = request.json.get('url')
    if not url:
        return jsonify({'error': 'URL is required'}), 400

    try:
        return jsonify(handle_scrapers(url))
    except Exception as e:
        return jsonify(({'error': str(e)})), 500


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5858)
