from flask import Flask, jsonify, request
from werkzeug.exceptions import HTTPException
from flask_cors import CORS
import json
import os

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": os.environ.get('MOCK_API_CORS_ORIGINS', '*')}})

@app.errorhandler(Exception)
def handle_unexpected_error(error):
    if isinstance(error, HTTPException):
        return error
    app.logger.exception("unhandled error while serving a request")
    return jsonify({"error": "internal server error"}), 500

# Load mock data
def load_mock_data(filename):
    with open(f'mock-data/{filename}', 'r') as f:
        return json.load(f)

@app.route('/data/QjX6hB1ST2IDKaxB/', methods=['GET'])
def get_data():
    try:
        # Default to users data for backward compatibility
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

if __name__ == '__main__':
    debug_enabled = os.environ.get('MOCK_API_DEBUG', '0') == '1'
    host = os.environ.get('MOCK_API_HOST', '127.0.0.1')
    port = int(os.environ.get('MOCK_API_PORT', '5000'))
    app.run(host=host, port=port, debug=debug_enabled, use_reloader=False)