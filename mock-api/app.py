from flask import Flask, jsonify
from flask_cors import CORS
import json
import logging
import os

# Mock-data filenames are hard-coded per endpoint: no request value ever reaches
# load_mock_data(), so the f-string below cannot be turned into path traversal.
ALLOWED_MOCK_FILES = ('users.json', 'pemanfaatan.json')

# CORS is restricted to explicit dev origins. A bare CORS(app) reflects any
# Origin, which lets any website read resident data from this API.
DEFAULT_ALLOWED_ORIGINS = (
    'http://localhost:8081',
    'http://127.0.0.1:8081',
    'http://localhost:3000',
    'http://127.0.0.1:3000',
)

app = Flask(__name__)
app.logger.setLevel(logging.INFO)

_allowed_origins = tuple(
    origin.strip()
    for origin in os.environ.get('MOCK_API_ALLOWED_ORIGINS', ','.join(DEFAULT_ALLOWED_ORIGINS)).split(',')
    if origin.strip()
)
CORS(
    app,
    resources={r'/data/*': {'origins': list(_allowed_origins)}},
    allow_credentials=False,
)


# Load mock data
def load_mock_data(filename):
    if filename not in ALLOWED_MOCK_FILES:
        raise ValueError(f'refusing to load unknown mock file: {filename!r}')
    with open(f'mock-data/{filename}', 'r') as f:
        return json.load(f)


def _serve(filename):
    """Serve a mock-data file, logging failures instead of leaking them to the client."""
    try:
        return jsonify(load_mock_data(filename))
    except FileNotFoundError:
        app.logger.exception('mock data file missing: %s', filename)
        return jsonify({'error': 'data unavailable'}), 503
    except Exception:
        # str(e) previously exposed absolute server paths and tracebacks.
        app.logger.exception('unexpected error while serving %s', filename)
        return jsonify({'error': 'internal error'}), 500


@app.route('/data/QjX6hB1ST2IDKaxB/', methods=['GET'])
def get_data():
    # Default to users data for backward compatibility
    return _serve('users.json')


@app.route('/data/QjX6hB1ST2IDKaxB/users', methods=['GET'])
def get_users():
    return _serve('users.json')


@app.route('/data/QjX6hB1ST2IDKaxB/pemanfaatan', methods=['GET'])
def get_pemanfaatan():
    return _serve('pemanfaatan.json')


if __name__ == '__main__':
    # The Werkzeug debugger is an unauthenticated-by-default Python console; it must
    # never be on outside an explicit, developer-machine opt-in.
    debug_enabled = os.environ.get('MOCK_API_DEBUG', '').lower() in ('1', 'true', 'yes')
    if debug_enabled:
        app.logger.warning('MOCK_API_DEBUG is enabled: Werkzeug debugger console is exposed')

    app.run(
        host=os.environ.get('MOCK_API_HOST', '0.0.0.0'),
        port=int(os.environ.get('MOCK_API_PORT', '5000')),
        debug=debug_enabled,
    )