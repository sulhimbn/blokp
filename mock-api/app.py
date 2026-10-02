"""Mock API server for BlokP.

Serves every endpoint the Android app calls under ``/data/<spreadsheet-id>/`` so the
client can be exercised end to end without the real spreadsheet backend.
"""
from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import os

app = Flask(__name__)
CORS(app)

SPREADSHEET_ID = os.environ.get("SPREADSHEET_ID", "QjX6hB1ST2IDKaxB")
BASE = f"/data/{SPREADSHEET_ID}"
DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "mock-data")


def load_mock_data(filename, default=None):
    path = os.path.join(DATA_DIR, filename)
    if not os.path.exists(path):
        return default
    with open(path, "r") as f:
        return json.load(f)


def read_collection(filename, key):
    payload = load_mock_data(filename, {})
    return payload.get(key, [])


@app.route(BASE + "/", methods=["GET"])
def get_data():
    return jsonify(load_mock_data("users.json", {"data": []}))


@app.route(BASE + "/users", methods=["GET"])
def get_users():
    return jsonify(load_mock_data("users.json", {"data": []}))


@app.route(BASE + "/pemanfaatan", methods=["GET"])
def get_pemanfaatan():
    return jsonify(load_mock_data("pemanfaatan.json", {"data": []}))


@app.route(BASE + "/announcements", methods=["GET"])
def get_announcements():
    return jsonify(read_collection("announcements.json", "data"))


@app.route(BASE + "/vendors", methods=["GET"])
def get_vendors():
    return jsonify({"data": read_collection("vendors.json", "data")})


@app.route(BASE + "/vendors/<vendor_id>", methods=["GET"])
def get_vendor(vendor_id):
    for vendor in read_collection("vendors.json", "data"):
        if vendor.get("id") == vendor_id:
            return jsonify({"data": vendor})
    return jsonify({"error": "Vendor not found"}), 404


@app.route(BASE + "/work-orders", methods=["GET"])
def get_work_orders():
    return jsonify({"data": read_collection("work-orders.json", "data")})


@app.route(BASE + "/work-orders/<work_order_id>", methods=["GET"])
def get_work_order(work_order_id):
    for order in read_collection("work-orders.json", "data"):
        if order.get("id") == work_order_id:
            return jsonify({"data": order})
    return jsonify({"error": "Work order not found"}), 404


@app.route(BASE + "/community-posts", methods=["GET"])
def get_community_posts():
    return jsonify(read_collection("community-posts.json", "data"))


@app.route(BASE + "/messages", methods=["GET"])
def get_messages():
    user_id = request.args.get("userId")
    if not user_id:
        return jsonify({"error": "userId query parameter is required"}), 400
    messages = [m for m in read_collection("messages.json", "data")
                if user_id in (m.get("senderId"), m.get("receiverId"))]
    return jsonify(messages)


@app.route(BASE + "/messages/<other_user_id>", methods=["GET"])
def get_messages_with_user(other_user_id):
    sender_id = request.args.get("senderId")
    if not sender_id:
        return jsonify({"error": "senderId query parameter is required"}), 400
    messages = [m for m in read_collection("messages.json", "data")
                if m.get("senderId") == sender_id and m.get("receiverId") == other_user_id]
    return jsonify(messages)


@app.route(BASE + "/messages", methods=["POST"])
def send_message():
    payload = request.get_json(silent=True) or {}
    sender_id = request.args.get("senderId")
    receiver_id = request.args.get("receiverId")
    content = request.args.get("content")
    if not (sender_id and receiver_id and content):
        return jsonify({"error": "senderId, receiverId and content are required"}), 400
    message = {
        "id": f"msg-{len(read_collection('messages.json', 'data')) + 1}",
        "senderId": sender_id,
        "receiverId": receiver_id,
        "content": content,
        "timestamp": payload.get("timestamp", "2024-01-01T00:00:00Z"),
        "readStatus": False,
        "attachments": [],
    }
    return jsonify(message), 201


@app.route(BASE + "/payments/initiate", methods=["POST"])
def initiate_payment():
    for field in ("amount", "description", "customerId", "paymentMethod"):
        if not request.args.get(field):
            return jsonify({"error": f"{field} is required"}), 400
    return jsonify({
        "transactionId": "txn-mock-1",
        "status": "PENDING",
        "paymentMethod": request.args.get("paymentMethod"),
        "amount": request.args.get("amount"),
        "currency": "IDR",
        "transactionTime": 1704067200000,
        "referenceNumber": "REF-MOCK-1",
    }), 201


@app.route(BASE + "/payments/<transaction_id>/status", methods=["GET"])
def get_payment_status(transaction_id):
    return jsonify({
        "transactionId": transaction_id,
        "status": "COMPLETED",
        "amount": "150000",
        "currency": "IDR",
        "updatedAt": 1704067200000,
    })


@app.route(BASE + "/payments/<transaction_id>/confirm", methods=["POST"])
def confirm_payment(transaction_id):
    return jsonify({
        "transactionId": transaction_id,
        "status": "CONFIRMED",
        "confirmationTime": 1704067200000,
    })


@app.errorhandler(404)
def not_found(_):
    return jsonify({"error": "Not found"}), 404


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5000)), debug=False)
