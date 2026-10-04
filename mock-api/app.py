import json
import os
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)

# This is a local development test double. Lock CORS down to the development
# origins the app is served from instead of reflecting every origin, so a page
# on any site cannot read resident data through a developer's localhost server.
ALLOWED_ORIGINS = [
    o.strip()
    for o in os.environ.get(
        "MOCK_API_ALLOWED_ORIGINS",
        "http://localhost,http://127.0.0.1,http://10.0.2.2,http://0.0.0.0",
    ).split(",")
    if o.strip()
]
CORS(app, resources={r"/data/*": {"origins": ALLOWED_ORIGINS}}, supports_credentials=False)


def load_mock_data(filename):
    with open(os.path.join(os.path.dirname(__file__), "mock-data", filename)) as f:
        return json.load(f)


def _register(path, filename):
    def handler():
        try:
            return jsonify(load_mock_data(filename))
        except FileNotFoundError:
            return jsonify({"error": "dataset not found"}), 404
        except Exception:
            return jsonify({"error": "failed to load dataset"}), 500

    app.add_url_rule(
        path,
        endpoint="serve_" + filename.replace(".json", "") + "_" + str(abs(hash(path))),
        view_func=handler,
        methods=["GET"],
    )


_register("/data/QjX6hB1ST2IDKaxB/", "users.json")
_register("/data/QjX6hB1ST2IDKaxB/users", "users.json")
_register("/data/QjX6hB1ST2IDKaxB/pemanfaatan", "pemanfaatan.json")


@app.errorhandler(500)
def handle_unexpected_error(e):
    return jsonify({"error": "internal error"}), 500


if __name__ == "__main__":
    # debug=False: the Werkzeug interactive debugger exposes a Python console and
    # prints its PIN to stdout, which must never be reachable on a shared network.
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "5000")), debug=False)
