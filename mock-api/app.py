from flask import Flask, jsonify
from flask_cors import CORS
import json
import os

app = Flask(__name__)

ALLOWED_ORIGINS = [
    origin.strip()
    for origin in os.environ.get('MOCK_API_ALLOWED_ORIGINS', '').split(',')
    if origin.strip()
]
if ALLOWED_ORIGINS:
    CORS(app, resources={r'/data/*': {'origins': ALLOWED_ORIGINS}})

BASE_DIR = os.path.dirname(os.path.abspath(__file__))


def load_mock_data(filename):
    with open(os.path.join(BASE_DIR, 'mock-data', filename), 'r') as f:
        return json.load(f)


@app.route('/data/QjX6hB1ST2IDKaxB/', methods=['GET'])
def get_data():
    try:
        users = load_mock_data('users.json')
        return jsonify(users)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/data/QjX6hB1ST2IDKaxB/users', methods=['GET'])
def get_users():
    try:
        users = load_mock_data('users.json')
        return jsonify(users)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/data/QjX6hB1ST2IDKaxB/pemanfaatan', methods=['GET'])
def get_pemanfaatan():
    try:
        pemanfaatan = load_mock_data('pemanfaatan.json')
        return jsonify(pemanfaatan)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok"})


if __name__ == '__main__':
    app.run(
        host=os.environ.get('MOCK_API_HOST', '127.0.0.1'),
        port=int(os.environ.get('MOCK_API_PORT', '5000')),
        debug=os.environ.get('MOCK_API_DEBUG', '').lower() == 'true',
    )