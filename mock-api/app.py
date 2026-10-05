from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import os

app = Flask(__name__)
CORS(app)

# Fixtures must resolve from __file__, not the process CWD, or the service 500s when
# started from anywhere other than mock-api/.
DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mock-data')

# Load mock data
def load_mock_data(filename):
    path = os.path.join(DATA_DIR, filename)
    with open(path, 'r') as f:
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
    app.run(host='0.0.0.0', port=5000, debug=True)