"""Mock API for BlokP.

Serves every endpoint declared in
app/src/main/java/com/example/iurankomplek/network/ApiService.kt
so that debug builds and contract tests have a real backend to talk to.

Response bodies match the Kotlin response models exactly
(UserResponse, PemanfaatanResponse, Announcement, Message,
CommunityPost, PaymentResponse, PaymentStatusResponse,
PaymentConfirmationResponse, VendorResponse, SingleVendorResponse,
WorkOrderResponse, SingleWorkOrderResponse).

State is kept in memory and resets on restart, which is what a dev
fixture server should do.
"""
from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import os
import threading
import time

app = Flask(__name__)
CORS(app)

SPREADSHEET_ID = "QjX6hB1ST2IDKaxB"
BASE = f"/data/{SPREADSHEET_ID}"
DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "mock-data")

_lock = threading.Lock()


def load_mock_data(filename):
    with open(os.path.join(DATA_DIR, filename), "r") as handle:
        return json.load(handle)


def now_iso():
    return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())


def error(message, status):
    return jsonify({"error": message}), status


# --------------------------------------------------------------------------
# Seeded in-memory state
# --------------------------------------------------------------------------

def seed_state():
    return {
        "announcements": [
            {
                "id": "ann-1",
                "title": "Pemutihan iuran Q3",
                "content": "Iuran Q3 dapat dibayar hingga akhir bulan.",
                "category": "payment",
                "priority": "high",
                "createdAt": "2026-09-01T08:00:00Z",
                "readBy": ["user-1"],
            },
            {
                "id": "ann-2",
                "title": "Pemeliharaan air bersih",
                "content": "Pemeliharaan dijadwalkan hari Minggu.",
                "category": "maintenance",
                "priority": "medium",
                "createdAt": "2026-09-15T08:00:00Z",
                "readBy": [],
            },
        ],
        "messages": [
            {
                "id": "msg-1",
                "senderId": "user-1",
                "receiverId": "user-2",
                "content": "Iuran bulan ini sudah saya transfer.",
                "timestamp": "2026-09-20T10:00:00Z",
                "readStatus": True,
                "attachments": [],
            }
        ],
        "community-posts": [
            {
                "id": "post-1",
                "authorId": "user-1",
                "title": "Usulan penataan jalan",
                "content": "Mohon penataan ulang jalan blok B.",
                "category": "infrastructure",
                "likes": 12,
                "comments": [
                    {
                        "id": "cmt-1",
                        "authorId": "user-2",
                        "content": "Setuju, moga cepat dikerjakan.",
                        "timestamp": "2026-09-18T12:00:00Z",
                    }
                ],
                "createdAt": "2026-09-17T09:00:00Z",
            }
        ],
        "payments": {},
        "vendors": [
            {
                "id": "v1",
                "name": "CV Nusantara Plumbing",
                "contactPerson": "Budi Santoso",
                "phoneNumber": "081234567890",
                "email": "budi@nusantara-plumbing.test",
                "specialty": "plumbing",
                "address": "Jl. Industri No. 5",
                "licenseNumber": "LIC-2026-001",
                "insuranceInfo": "AS-2026-778",
                "certifications": ["PIPA", "SLO"],
                "rating": 4.5,
                "totalReviews": 12,
                "contractStart": "2026-01-01",
                "contractEnd": "2026-12-31",
                "isActive": True,
            },
            {
                "id": "v2",
                "name": "Toko Listrik Sejahtera",
                "contactPerson": "Siti Rahayu",
                "phoneNumber": "081298765432",
                "email": "siti@sejahtera-listrik.test",
                "specialty": "electrical",
                "address": "Jl. Merdeka No. 9",
                "licenseNumber": "LIC-2025-114",
                "insuranceInfo": "AS-2025-442",
                "certifications": ["SLO"],
                "rating": 4.1,
                "totalReviews": 7,
                "contractStart": "2025-06-01",
                "contractEnd": "2026-05-31",
                "isActive": False,
            },
        ],
        "work_orders": [
            {
                "id": "wo1",
                "title": "Pipa air bocor",
                "description": "Pipa air di sisi timur blok C bocor parah.",
                "category": "plumbing",
                "priority": "urgent",
                "status": "assigned",
                "vendorId": "v1",
                "vendorName": "CV Nusantara Plumbing",
                "assignedAt": "2026-09-19T09:00:00Z",
                "scheduledDate": "2026-10-05",
                "completedAt": None,
                "estimatedCost": 750000.0,
                "actualCost": 0.0,
                "propertyId": "blok-c",
                "reporterId": "user-2",
                "createdAt": "2026-09-18T08:00:00Z",
                "updatedAt": "2026-09-19T09:00:00Z",
                "attachments": ["https://example.test/wo1.jpg"],
                "notes": [],
            }
        ],
        "counters": {"vendor": 3, "work_order": 2, "message": 2, "post": 2,
                     "payment": 1},
    }


STATE = seed_state()


def next_id(kind, prefix):
    with _lock:
        n = STATE["counters"][kind]
        STATE["counters"][kind] = n + 1
    return f"{prefix}{n}"


def require_params(*names):
    """Return (params_dict, error_response). error_response is None when OK."""
    missing = [n for n in names if not request.args.get(n)]
    if missing:
        return None, error(f"missing query parameters: {', '.join(missing)}", 400)
    return request.args, None


# --------------------------------------------------------------------------
# Legacy spreadsheet endpoints
# --------------------------------------------------------------------------

@ app.route(f"{BASE}/", methods=["GET"])
def get_data():
    try:
        return jsonify(load_mock_data("users.json"))
    except Exception as exc:  # noqa: BLE001
        return error(str(exc), 500)


@ app.route(f"{BASE}/users", methods=["GET"])
def get_users():
    try:
        return jsonify(load_mock_data("users.json"))
    except Exception as exc:  # noqa: BLE001
        return error(str(exc), 500)


@ app.route(f"{BASE}/pemanfaatan", methods=["GET"])
def get_pemanfaatan():
    try:
        return jsonify(load_mock_data("pemanfaatan.json"))
    except Exception as exc:  # noqa: BLE001
        return error(str(exc), 500)


# --------------------------------------------------------------------------
# Communication
# --------------------------------------------------------------------------

@ app.route(f"{BASE}/announcements", methods=["GET"])
def get_announcements():
    return jsonify(STATE["announcements"])


@ app.route(f"{BASE}/messages", methods=["GET", "POST"])
def messages():
    if request.method == "GET":
        user_id = request.args.get("userId")
        if not user_id:
            return error("missing query parameter: userId", 400)
        rows = [m for m in STATE["messages"]
                if m["senderId"] == user_id or m["receiverId"] == user_id]
        return jsonify(rows)

    args, err = require_params("senderId", "receiverId", "content")
    if err:
        return err
    message = {
        "id": next_id("message", "msg-"),
        "senderId": args["senderId"],
        "receiverId": args["receiverId"],
        "content": args["content"],
        "timestamp": now_iso(),
        "readStatus": False,
        "attachments": [],
    }
    STATE["messages"].append(message)
    return jsonify(message), 201


@ app.route(f"{BASE}/messages/<receiver_id>", methods=["GET"])
def messages_with_user(receiver_id):
    sender_id = request.args.get("senderId")
    if not sender_id:
        return error("missing query parameter: senderId", 400)
    rows = [m for m in STATE["messages"]
            if m["receiverId"] == receiver_id and m["senderId"] == sender_id]
    return jsonify(rows)


@ app.route(f"{BASE}/community-posts", methods=["GET", "POST"])
def community_posts():
    if request.method == "GET":
        return jsonify(STATE["community-posts"])

    args, err = require_params("authorId", "title", "content", "category")
    if err:
        return err
    post = {
        "id": next_id("post", "post-"),
        "authorId": args["authorId"],
        "title": args["title"],
        "content": args["content"],
        "category": args["category"],
        "likes": 0,
        "comments": [],
        "createdAt": now_iso(),
    }
    STATE["community-posts"].append(post)
    return jsonify(post), 201


# --------------------------------------------------------------------------
# Payments
# --------------------------------------------------------------------------

@ app.route(f"{BASE}/payments/initiate", methods=["POST"])
def initiate_payment():
    args, err = require_params("amount", "description", "customerId", "paymentMethod")
    if err:
        return err
    try:
        amount = float(args["amount"])
    except ValueError:
        return error("amount must be numeric", 400)
    if amount <= 0:
        return error("amount must be positive", 400)

    txn = next_id("payment", "txn-")
    payment = {
        "transactionId": txn,
        "status": "PENDING",
        "paymentMethod": args["paymentMethod"],
        "amount": args["amount"],
        "currency": "IDR",
        "transactionTime": int(time.time() * 1000),
        "referenceNumber": f"REF-{txn}",
    }
    STATE["payments"][txn] = {"amount": args["amount"], "status": "PENDING",
                              "createdAt": int(time.time() * 1000)}
    return jsonify(payment), 201


@ app.route(f"{BASE}/payments/<payment_id>/status", methods=["GET"])
def payment_status(payment_id):
    payment = STATE["payments"].get(payment_id)
    if not payment:
        return error(f"payment {payment_id} not found", 404)
    return jsonify({
        "transactionId": payment_id,
        "status": payment["status"],
        "amount": payment["amount"],
        "currency": "IDR",
        "updatedAt": int(time.time() * 1000),
    })


@ app.route(f"{BASE}/payments/<payment_id>/confirm", methods=["POST"])
def confirm_payment(payment_id):
    payment = STATE["payments"].get(payment_id)
    if not payment:
        return error(f"payment {payment_id} not found", 404)
    if payment["status"] == "COMPLETED":
        return error("payment already confirmed", 409)
    payment["status"] = "COMPLETED"
    return jsonify({
        "transactionId": payment_id,
        "status": "COMPLETED",
        "confirmationTime": int(time.time() * 1000),
    })


# --------------------------------------------------------------------------
# Vendors
# --------------------------------------------------------------------------

VENDOR_FIELDS = ("name", "contactPerson", "phoneNumber", "email", "specialty",
                 "address", "licenseNumber", "insuranceInfo",
                 "contractStart", "contractEnd")


def _vendor_payload(args, existing=None):
    vendor = dict(existing) if existing else {
        "id": None, "certifications": [], "rating": 0.0, "totalReviews": 0,
    }
    for field in VENDOR_FIELDS:
        vendor[field] = args[field]
    return vendor


@ app.route(f"{BASE}/vendors", methods=["GET", "POST"])
def vendors():
    if request.method == "GET":
        return jsonify({"data": STATE["vendors"]})

    args, err = require_params(*VENDOR_FIELDS)
    if err:
        return err
    vendor = _vendor_payload(args)
    vendor["id"] = next_id("vendor", "v")
    vendor["isActive"] = True
    STATE["vendors"].append(vendor)
    return jsonify({"data": vendor}), 201


@ app.route(f"{BASE}/vendors/<vendor_id>", methods=["GET", "PUT"])
def vendor_detail(vendor_id):
    index = next((i for i, v in enumerate(STATE["vendors"]) if v["id"] == vendor_id), None)
    if index is None:
        return error(f"vendor {vendor_id} not found", 404)

    if request.method == "GET":
        return jsonify({"data": STATE["vendors"][index]})

    args, err = require_params(*VENDOR_FIELDS, "isActive")
    if err:
        return err
    is_active = str(args["isActive"]).lower() not in ("false", "0", "no")
    vendor = _vendor_payload(args, existing=STATE["vendors"][index])
    vendor["isActive"] = is_active
    STATE["vendors"][index] = vendor
    return jsonify({"data": vendor})


# --------------------------------------------------------------------------
# Work orders
# --------------------------------------------------------------------------

WORK_ORDER_FIELDS = ("title", "description", "category", "priority",
                     "propertyId", "reporterId", "estimatedCost")


@ app.route(f"{BASE}/work-orders", methods=["GET", "POST"])
def work_orders():
    if request.method == "GET":
        return jsonify({"data": STATE["work_orders"]})

    args, err = require_params(*WORK_ORDER_FIELDS)
    if err:
        return err
    try:
        estimated_cost = float(args["estimatedCost"])
    except ValueError:
        return error("estimatedCost must be numeric", 400)

    work_order = {
        "id": next_id("work_order", "wo"),
        "title": args["title"],
        "description": args["description"],
        "category": args["category"],
        "priority": args["priority"],
        "status": "pending",
        "vendorId": None,
        "vendorName": None,
        "assignedAt": None,
        "scheduledDate": None,
        "completedAt": None,
        "estimatedCost": estimated_cost,
        "actualCost": 0.0,
        "propertyId": args["propertyId"],
        "reporterId": args["reporterId"],
        "createdAt": now_iso(),
        "updatedAt": now_iso(),
        "attachments": [],
        "notes": [],
    }
    STATE["work_orders"].append(work_order)
    return jsonify({"data": work_order}), 201


def _find_work_order(work_order_id):
    return next((w for w in STATE["work_orders"] if w["id"] == work_order_id), None)


@ app.route(f"{BASE}/work-orders/<work_order_id>", methods=["GET"])
def work_order_detail(work_order_id):
    work_order = _find_work_order(work_order_id)
    if work_order is None:
        return error(f"work order {work_order_id} not found", 404)
    return jsonify({"data": work_order})


@ app.route(f"{BASE}/work-orders/<work_order_id>/assign", methods=["PUT"])
def assign_work_order(work_order_id):
    work_order = _find_work_order(work_order_id)
    if work_order is None:
        return error(f"work order {work_order_id} not found", 404)

    args, err = require_params("vendorId")
    if err:
        return err
    vendor = next((v for v in STATE["vendors"] if v["id"] == args["vendorId"]), None)
    if vendor is None:
        return error(f"vendor {args['vendorId']} not found", 404)

    work_order["vendorId"] = vendor["id"]
    work_order["vendorName"] = vendor["name"]
    work_order["assignedAt"] = now_iso()
    work_order["scheduledDate"] = args.get("scheduledDate") or work_order["scheduledDate"]
    work_order["status"] = "assigned"
    work_order["updatedAt"] = now_iso()
    return jsonify({"data": work_order})


@ app.route(f"{BASE}/work-orders/<work_order_id>/status", methods=["PUT"])
def update_work_order_status(work_order_id):
    work_order = _find_work_order(work_order_id)
    if work_order is None:
        return error(f"work order {work_order_id} not found", 404)

    args, err = require_params("status")
    if err:
        return err
    status = args["status"]
    if status not in ("pending", "assigned", "in_progress", "completed", "cancelled"):
        return error(f"invalid status: {status}", 400)

    work_order["status"] = status
    if status == "completed":
        work_order["completedAt"] = now_iso()
    notes = args.get("notes")
    if notes:
        work_order["notes"].append(notes)
    work_order["updatedAt"] = now_iso()
    return jsonify({"data": work_order})


@ app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "endpoints": len(list(app.url_map.iter_rules()))})


@ app.route("/reset", methods=["POST"])
def reset():
    STATE.clear()
    STATE.update(seed_state())
    return jsonify({"status": "reset"})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
