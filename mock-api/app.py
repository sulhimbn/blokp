from flask import Flask, jsonify, request
from flask_cors import CORS
import copy
import itertools
import json
import os
import time
import uuid

app = Flask(__name__)
CORS(app)

SPREADSHEET_ID = "QjX6hB1ST2IDKaxB"
BASE = f"/data/{SPREADSHEET_ID}"

_ids = itertools.count(1)


def load_mock_data(filename):
    with open(f"mock-data/{filename}", "r") as f:
        return json.load(f)


def new_id(prefix):
    return f"{prefix}_{next(_ids):04d}"


def now_ms():
    return int(time.time() * 1000)


def store():
    if "store" not in app.config:
        app.config["store"] = {
            "announcements": load_mock_data("users.json"),
            "vendors": {"data": []},
            "workOrders": {"data": []},
            "messages": [],
            "communityPosts": [],
            "payments": {},
        }
    return app.config["store"]


def seed():
    s = store()
    if s["vendors"]["data"]:
        return
    s["vendors"]["data"] = [
        {
            "id": "V001",
            "name": "Acme Plumbing",
            "contactPerson": "Budi Santoso",
            "phoneNumber": "081234567890",
            "email": "contact@acmeplumbing.example",
            "specialty": "plumbing",
            "address": "Jl. Merdeka No. 12",
            "licenseNumber": "PL-2024-001",
            "insuranceInfo": "Asuransianggung liability 2024",
            "certifications": ["Licensed", "Bonded"],
            "rating": 4.5,
            "totalReviews": 25,
            "contractStart": "2024-01-01",
            "contractEnd": "2025-12-31",
            "isActive": True,
        },
        {
            "id": "V002",
            "name": "SecureTech CCTV",
            "contactPerson": "Andi Wijaya",
            "phoneNumber": "081298765432",
            "email": "ops@securetech.example",
            "specialty": "security",
            "address": "Jl. Agung No. 8",
            "licenseNumber": "SC-2024-014",
            "insuranceInfo": "Asuransi fullest 2024",
            "certifications": ["Certified"],
            "rating": 4.8,
            "totalReviews": 41,
            "contractStart": "2024-03-01",
            "contractEnd": "2025-02-28",
            "isActive": True,
        },
    ]
    s["workOrders"]["data"] = [
        {
            "id": "WO001",
            "title": "Pipa bocor di dapur",
            "description": "Pipa air bocor di bawah sink unit B-12",
            "category": "plumbing",
            "priority": "high",
            "status": "pending",
            "vendorId": None,
            "vendorName": None,
            "assignedAt": None,
            "scheduledDate": None,
            "completedAt": None,
            "estimatedCost": 450000.0,
            "actualCost": 0.0,
            "propertyId": "B-12",
            "reporterId": "john@example.com",
            "createdAt": "2024-05-01T08:00:00Z",
            "updatedAt": "2024-05-01T08:00:00Z",
            "attachments": [],
            "notes": [],
        }
    ]
    s["communityPosts"] = [
        {
            "id": "P001",
            "authorId": "john@example.com",
            "title": "Rapat warga",
            "content": "Rapat warga dominion Minggu pukul 09:00 di aula.",
            "category": "event",
            "likes": 4,
            "comments": [
                {
                    "id": "C001",
                    "authorId": "jane@example.com",
                    "content": "Hadir",
                    "timestamp": "2024-05-02T10:00:00Z",
                }
            ],
            "createdAt": "2024-05-02T07:00:00Z",
        }
    ]


def require_fields(*names):
    missing = [n for n in names if not request.args.get(n)]
    if missing:
        return jsonify({"error": f"missing required query parameters: {', '.join(missing)}"}), 400
    return None


@app.route(f"{BASE}/", methods=["GET"])
@app.route(f"{BASE}/users", methods=["GET"])
def get_users():
    return jsonify(load_mock_data("users.json"))


@app.route(f"{BASE}/pemanfaatan", methods=["GET"])
def get_pemanfaatan():
    return jsonify(load_mock_data("pemanfaatan.json"))


# ---------------- announcements ----------------

@app.route(f"{BASE}/announcements", methods=["GET"])
def get_announcements():
    return jsonify([
        {
            "id": "A001",
            "title": "Pemadatan listrik terjadwal",
            "content": "Pemadatan listrik Blok A-B berlangsung 09:00-13:00.",
            "category": "utility",
            "priority": "high",
            "createdAt": "2024-05-10T02:00:00Z",
            "readBy": [],
        }
    ])


# ---------------- messages ----------------

@app.route(f"{BASE}/messages", methods=["GET"])
def get_messages():
    user_id = request.args.get("userId")
    s = store()
    return jsonify([m for m in s["messages"] if user_id in (m["senderId"], m["receiverId"])])


@app.route(f"{BASE}/messages/<receiver_id>", methods=["GET"])
def get_messages_with_user(receiver_id):
    sender_id = request.args.get("senderId")
    s = store()
    return jsonify([
        m for m in s["messages"]
        if {m["senderId"], m["receiverId"]} == {sender_id, receiver_id}
    ])


@app.route(f"{BASE}/messages", methods=["POST"])
def send_message():
    err = require_fields("senderId", "receiverId", "content")
    if err:
        return err
    message = {
        "id": new_id("MSG"),
        "senderId": request.args["senderId"],
        "receiverId": request.args["receiverId"],
        "content": request.args["content"],
        "timestamp": "2024-05-11T09:00:00Z",
        "readStatus": False,
        "attachments": [],
    }
    store()["messages"].append(message)
    return jsonify(message), 201


# ---------------- community ----------------

@app.route(f"{BASE}/community-posts", methods=["GET"])
def get_community_posts():
    seed()
    return jsonify(store()["communityPosts"])


@app.route(f"{BASE}/community-posts", methods=["POST"])
def create_community_post():
    err = require_fields("authorId", "title", "content", "category")
    if err:
        return err
    seed()
    post = {
        "id": new_id("POST"),
        "authorId": request.args["authorId"],
        "title": request.args["title"],
        "content": request.args["content"],
        "category": request.args["category"],
        "likes": 0,
        "comments": [],
        "createdAt": "2024-05-11T10:00:00Z",
    }
    store()["communityPosts"].append(post)
    return jsonify(post), 201


# ---------------- payments ----------------

@app.route(f"{BASE}/payments/initiate", methods=["POST"])
def initiate_payment():
    err = require_fields("amount", "description", "customerId", "paymentMethod")
    if err:
        return err
    txn = new_id("TXN")
    store()["payments"][txn] = {
        "transactionId": txn,
        "status": "PENDING",
        "paymentMethod": request.args["paymentMethod"],
        "amount": request.args["amount"],
        "currency": "IDR",
        "transactionTime": now_ms(),
        "referenceNumber": new_id("REF"),
    }
    return jsonify(store()["payments"][txn]), 201


@app.route(f"{BASE}/payments/<payment_id>/status", methods=["GET"])
def get_payment_status(payment_id):
    payment = store()["payments"].get(payment_id)
    if not payment:
        return jsonify({"error": "payment not found"}), 404
    return jsonify({
        "transactionId": payment["transactionId"],
        "status": payment["status"],
        "amount": payment["amount"],
        "currency": payment["currency"],
        "updatedAt": now_ms(),
    })


@app.route(f"{BASE}/payments/<payment_id>/confirm", methods=["POST"])
def confirm_payment(payment_id):
    payment = store()["payments"].get(payment_id)
    if not payment:
        return jsonify({"error": "payment not found"}), 404
    payment["status"] = "CONFIRMED"
    return jsonify({
        "transactionId": payment["transactionId"],
        "status": "CONFIRMED",
        "confirmationTime": now_ms(),
    })


# ---------------- vendors ----------------

@app.route(f"{BASE}/vendors", methods=["GET"])
def get_vendors():
    seed()
    return jsonify(store()["vendors"])


@app.route(f"{BASE}/vendors/<vendor_id>", methods=["GET"])
def get_vendor(vendor_id):
    seed()
    for vendor in store()["vendors"]["data"]:
        if vendor["id"] == vendor_id:
            return jsonify({"data": vendor})
    return jsonify({"error": "vendor not found"}), 404


@app.route(f"{BASE}/vendors", methods=["POST"])
def create_vendor():
    err = require_fields(
        "name", "contactPerson", "phoneNumber", "email", "specialty",
        "address", "licenseNumber", "insuranceInfo", "contractStart", "contractEnd",
    )
    if err:
        return err
    seed()
    vendor = {
        "id": new_id("V"),
        "name": request.args["name"],
        "contactPerson": request.args["contactPerson"],
        "phoneNumber": request.args["phoneNumber"],
        "email": request.args["email"],
        "specialty": request.args["specialty"],
        "address": request.args["address"],
        "licenseNumber": request.args["licenseNumber"],
        "insuranceInfo": request.args["insuranceInfo"],
        "certifications": [],
        "rating": 0.0,
        "totalReviews": 0,
        "contractStart": request.args["contractStart"],
        "contractEnd": request.args["contractEnd"],
        "isActive": True,
    }
    store()["vendors"]["data"].append(vendor)
    return jsonify({"data": vendor}), 201


@app.route(f"{BASE}/vendors/<vendor_id>", methods=["PUT"])
def update_vendor(vendor_id):
    seed()
    for vendor in store()["vendors"]["data"]:
        if vendor["id"] == vendor_id:
            for field in (
                "name", "contactPerson", "phoneNumber", "email", "specialty",
                "address", "licenseNumber", "insuranceInfo", "contractStart", "contractEnd",
            ):
                if request.args.get(field):
                    vendor[field] = request.args[field]
            if request.args.get("isActive") is not None:
                vendor["isActive"] = request.args["isActive"].lower() == "true"
            return jsonify({"data": vendor})
    return jsonify({"error": "vendor not found"}), 404


# ---------------- work orders ----------------

@app.route(f"{BASE}/work-orders", methods=["GET"])
def get_work_orders():
    seed()
    return jsonify(store()["workOrders"])


@app.route(f"{BASE}/work-orders/<work_order_id>", methods=["GET"])
def get_work_order(work_order_id):
    seed()
    for wo in store()["workOrders"]["data"]:
        if wo["id"] == work_order_id:
            return jsonify({"data": wo})
    return jsonify({"error": "work order not found"}), 404


@app.route(f"{BASE}/work-orders", methods=["POST"])
def create_work_order():
    err = require_fields(
        "title", "description", "category", "priority", "propertyId", "reporterId",
    )
    if err:
        return err
    seed()
    wo = {
        "id": new_id("WO"),
        "title": request.args["title"],
        "description": request.args["description"],
        "category": request.args["category"],
        "priority": request.args["priority"],
        "status": "pending",
        "vendorId": None,
        "vendorName": None,
        "assignedAt": None,
        "scheduledDate": None,
        "completedAt": None,
        "estimatedCost": float(request.args.get("estimatedCost", 0)),
        "actualCost": 0.0,
        "propertyId": request.args["propertyId"],
        "reporterId": request.args["reporterId"],
        "createdAt": "2024-05-11T11:00:00Z",
        "updatedAt": "2024-05-11T11:00:00Z",
        "attachments": [],
        "notes": [],
    }
    store()["workOrders"]["data"].append(wo)
    return jsonify({"data": wo}), 201


def find_work_order(work_order_id):
    for wo in store()["workOrders"]["data"]:
        if wo["id"] == work_order_id:
            return wo
    return None


@app.route(f"{BASE}/work-orders/<work_order_id>/assign", methods=["PUT"])
def assign_vendor(work_order_id):
    seed()
    if not request.args.get("vendorId"):
        return jsonify({"error": "missing required query parameter: vendorId"}), 400
    wo = find_work_order(work_order_id)
    if not wo:
        return jsonify({"error": "work order not found"}), 404
    vendor_id = request.args["vendorId"]
    wo["vendorId"] = vendor_id
    wo["assignedAt"] = "2024-05-11T12:00:00Z"
    wo["scheduledDate"] = request.args.get("scheduledDate")
    wo["status"] = "assigned"
    for vendor in store()["vendors"]["data"]:
        if vendor["id"] == vendor_id:
            wo["vendorName"] = vendor["name"]
    return jsonify({"data": wo})


@app.route(f"{BASE}/work-orders/<work_order_id>/status", methods=["PUT"])
def update_work_order_status(work_order_id):
    seed()
    if not request.args.get("status"):
        return jsonify({"error": "missing required query parameter: status"}), 400
    wo = find_work_order(work_order_id)
    if not wo:
        return jsonify({"error": "work order not found"}), 404
    wo["status"] = request.args["status"]
    wo["updatedAt"] = "2024-05-11T13:00:00Z"
    if request.args.get("notes"):
        wo["notes"].append(request.args["notes"])
    return jsonify({"data": wo})


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "spreadsheetId": SPREADSHEET_ID})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False)