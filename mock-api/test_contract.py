#!/usr/bin/env python3
"""Contract tests for the BlokP mock API.

Every case mirrors one endpoint declared in
app/src/main/java/com/example/iurankomplek/network/ApiService.kt and asserts
that the served body carries the fields the matching Kotlin response model
requires, so a silent schema drift fails here instead of on a device.

Run:  python3 mock-api/test_contract.py
"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import app as mock  # noqa: E402

VENDOR_FIELDS = {
    "id", "name", "contactPerson", "phoneNumber", "email", "specialty",
    "address", "licenseNumber", "insuranceInfo", "certifications",
    "rating", "totalReviews", "contractStart", "contractEnd", "isActive",
}

WORK_ORDER_FIELDS = {
    "id", "title", "description", "category", "priority", "status",
    "vendorId", "vendorName", "assignedAt", "scheduledDate", "completedAt",
    "estimatedCost", "actualCost", "propertyId", "reporterId",
    "createdAt", "updatedAt", "attachments", "notes",
}

MESSAGE_FIELDS = {
    "id", "senderId", "receiverId", "content", "timestamp", "readStatus",
    "attachments",
}

ANNOUNCEMENT_FIELDS = {
    "id", "title", "content", "category", "priority", "createdAt", "readBy",
}

POST_FIELDS = {
    "id", "authorId", "title", "content", "category", "likes", "comments",
    "createdAt",
}

DATA_ITEM_FIELDS = {
    "first_name", "last_name", "email", "alamat", "iuran_perwarga",
    "total_iuran_rekap", "jumlah_iuran_bulanan", "total_iuran_individu",
    "pengeluaran_iuran_warga", "pemanfaatan_iuran", "avatar",
}

VENDOR_ARGS = {
    "name": "CV Test", "contactPerson": "Tester", "phoneNumber": "0800",
    "email": "t@test.id", "specialty": "plumbing", "address": "Jl. Test 1",
    "licenseNumber": "L-1", "insuranceInfo": "AS-1",
    "contractStart": "2026-01-01", "contractEnd": "2026-12-31",
}

BASE = mock.BASE


class ContractTest(unittest.TestCase):
    def setUp(self):
        mock.app.config["TESTING"] = True
        self.client = mock.app.test_client()

    def tearDown(self):
        mock.STATE.clear()
        mock.STATE.update(mock.seed_state())

    def get_json(self, path, expected=200):
        resp = self.client.get(BASE + path)
        self.assertEqual(resp.status_code, expected, f"GET {path}")
        return resp.get_json()

    def post_json(self, path, expected=201):
        resp = self.client.post(BASE + path)
        self.assertEqual(resp.status_code, expected, f"POST {path}")
        return resp.get_json()

    def put_json(self, path, expected=200):
        resp = self.client.put(BASE + path)
        self.assertEqual(resp.status_code, expected, f"PUT {path}")
        return resp.get_json()

    # ---- legacy spreadsheet endpoints ------------------------------------

    def test_users_endpoint_matches_user_response_model(self):
        body = self.get_json("/users")
        self.assertIn("data", body)
        self.assertTrue(body["data"])
        self.assertEqual(set(), DATA_ITEM_FIELDS - set(body["data"][0]))

    def test_pemanfaatan_endpoint_matches_pemanfaatan_response_model(self):
        body = self.get_json("/pemanfaatan")
        self.assertIn("data", body)
        self.assertTrue(body["data"])
        self.assertEqual(set(), DATA_ITEM_FIELDS - set(body["data"][0]))

    def test_legacy_root_endpoint_serves_users(self):
        body = self.get_json("/")
        self.assertIn("data", body)

    # ---- communication ----------------------------------------------------

    def test_get_announcements_matches_announcement_model(self):
        body = self.get_json("/announcements")
        self.assertIsInstance(body, list)
        self.assertTrue(body)
        self.assertEqual(set(), ANNOUNCEMENT_FIELDS - set(body[0]))

    def test_get_messages_filters_by_user(self):
        body = self.get_json("/messages?userId=user-1")
        self.assertIsInstance(body, list)
        self.assertTrue(body)
        self.assertEqual(set(), MESSAGE_FIELDS - set(body[0]))
        for msg in body:
            self.assertIn("user-1", (msg["senderId"], msg["receiverId"]))

    def test_get_messages_requires_user_id(self):
        self.assertEqual(self.client.get(BASE + "/messages").status_code, 400)

    def test_get_messages_with_user_filters_both_sides(self):
        body = self.get_json("/messages/user-2?senderId=user-1")
        self.assertEqual(len(body), 1)
        self.assertEqual(set(), MESSAGE_FIELDS - set(body[0]))

    def test_send_message_creates_message(self):
        msg = self.post_json(
            "/messages?senderId=user-1&receiverId=user-3&content=halo")
        self.assertEqual(set(), MESSAGE_FIELDS - set(msg))
        self.assertEqual("user-3", msg["receiverId"])
        self.assertFalse(msg["readStatus"])

    def test_send_message_validates_query(self):
        self.assertEqual(self.client.post(BASE + "/messages").status_code, 400)

    def test_get_community_posts_matches_model(self):
        body = self.get_json("/community-posts")
        self.assertIsInstance(body, list)
        self.assertTrue(body)
        self.assertEqual(set(), POST_FIELDS - set(body[0]))

    def test_create_community_post(self):
        post = self.post_json(
            "/community-posts?authorId=user-1&title=T&content=C&category=general")
        self.assertEqual(set(), POST_FIELDS - set(post))
        self.assertEqual(0, post["likes"])

    # ---- payments ---------------------------------------------------------

    def test_initiate_payment_matches_payment_response(self):
        body = self.post_json(
            "/payments/initiate?amount=150000&description=iuran"
            "&customerId=user-1&paymentMethod=bank_transfer")
        expected = {"transactionId", "status", "paymentMethod", "amount",
                    "currency", "transactionTime", "referenceNumber"}
        self.assertEqual(set(), expected - set(body))
        self.assertEqual("PENDING", body["status"])
        self.assertEqual("IDR", body["currency"])

    def test_initiate_payment_rejects_non_numeric_amount(self):
        resp = self.client.post(
            BASE + "/payments/initiate?amount=abc&description=d"
            "&customerId=u&paymentMethod=m")
        self.assertEqual(resp.status_code, 400)

    def test_initiate_payment_rejects_non_positive_amount(self):
        resp = self.client.post(
            BASE + "/payments/initiate?amount=0&description=d"
            "&customerId=u&paymentMethod=m")
        self.assertEqual(resp.status_code, 400)

    def test_payment_status_lifecycle(self):
        txn = self.post_json(
            "/payments/initiate?amount=1000&description=d"
            "&customerId=u&paymentMethod=bank_transfer")["transactionId"]

        status = self.get_json(f"/payments/{txn}/status")
        self.assertEqual(set(), {"transactionId", "status", "amount",
                                 "currency", "updatedAt"} - set(status))
        self.assertEqual("PENDING", status["status"])

        confirmed = self.post_json(f"/payments/{txn}/confirm", expected=200)
        self.assertEqual(set(), {"transactionId", "status",
                                 "confirmationTime"} - set(confirmed))
        self.assertEqual("COMPLETED", confirmed["status"])
        self.assertEqual("COMPLETED",
                         self.get_json(f"/payments/{txn}/status")["status"])

    def test_confirming_unknown_payment_is_404(self):
        self.assertEqual(self.client.post(BASE + "/payments/nope/confirm")
                         .status_code, 404)

    def test_double_confirmation_is_rejected(self):
        txn = self.post_json(
            "/payments/initiate?amount=1000&description=d"
            "&customerId=u&paymentMethod=m")["transactionId"]
        self.client.post(BASE + f"/payments/{txn}/confirm")
        self.assertEqual(self.client.post(BASE + f"/payments/{txn}/confirm")
                         .status_code, 409)

    # ---- vendors ----------------------------------------------------------

    def test_get_vendors_matches_vendor_response(self):
        body = self.get_json("/vendors")
        self.assertEqual(set(), VENDOR_FIELDS - set(body["data"][0]))

    def test_get_single_vendor(self):
        body = self.get_json("/vendors/v1")
        self.assertEqual(set(), VENDOR_FIELDS - set(body["data"]))

    def test_get_unknown_vendor_is_404(self):
        self.assertEqual(self.client.get(BASE + "/vendors/nope")
                         .status_code, 404)

    def test_create_vendor(self):
        body = self.post_json("/vendors?" + "&".join(
            f"{k}={v}" for k, v in VENDOR_ARGS.items()))
        self.assertEqual(set(), VENDOR_FIELDS - set(body["data"]))
        self.assertTrue(body["data"]["isActive"])

    def test_create_vendor_requires_all_params(self):
        self.assertEqual(self.client.post(BASE + "/vendors").status_code, 400)

    def test_update_vendor(self):
        query = "&".join(f"{k}={v}" for k, v in VENDOR_ARGS.items())
        body = self.put_json(f"/vendors/v1?{query}&isActive=false")
        self.assertEqual(set(), VENDOR_FIELDS - set(body["data"]))
        self.assertFalse(body["data"]["isActive"])

    def test_update_unknown_vendor_is_404(self):
        resp = self.client.put(BASE + "/vendors/nope?isActive=true")
        self.assertEqual(resp.status_code, 404)

    # ---- work orders ------------------------------------------------------

    def test_get_work_orders_matches_model(self):
        body = self.get_json("/work-orders")
        self.assertEqual(set(), WORK_ORDER_FIELDS - set(body["data"][0]))

    def test_get_single_work_order(self):
        body = self.get_json("/work-orders/wo1")
        self.assertEqual(set(), WORK_ORDER_FIELDS - set(body["data"]))

    def test_get_unknown_work_order_is_404(self):
        self.assertEqual(self.client.get(BASE + "/work-orders/nope")
                         .status_code, 404)

    def test_create_work_order_defaults_to_pending(self):
        body = self.post_json(
            "/work-orders?title=T&description=D&category=plumbing"
            "&priority=high&propertyId=blok-a&reporterId=user-1"
            "&estimatedCost=100.5")
        self.assertEqual(set(), WORK_ORDER_FIELDS - set(body["data"]))
        self.assertEqual("pending", body["data"]["status"])
        self.assertIsNone(body["data"]["vendorId"])
        self.assertEqual(100.5, body["data"]["estimatedCost"])

    def test_create_work_order_rejects_bad_cost(self):
        resp = self.client.post(
            BASE + "/work-orders?title=T&description=D&category=c&priority=p"
            "&propertyId=b&reporterId=r&estimatedCost=abc")
        self.assertEqual(resp.status_code, 400)

    def test_assign_vendor_sets_vendor_and_status(self):
        body = self.put_json(
            "/work-orders/wo1/assign?vendorId=v1&scheduledDate=2026-11-01")
        data = body["data"]
        self.assertEqual(set(), WORK_ORDER_FIELDS - set(data))
        self.assertEqual("v1", data["vendorId"])
        self.assertEqual("assigned", data["status"])
        self.assertIsNotNone(data["assignedAt"])

    def test_assign_unknown_vendor_is_404(self):
        resp = self.client.put(BASE + "/work-orders/wo1/assign?vendorId=nope")
        self.assertEqual(resp.status_code, 404)

    def test_assign_unknown_work_order_is_404(self):
        resp = self.client.put(BASE + "/work-orders/nope/assign?vendorId=v1")
        self.assertEqual(resp.status_code, 404)

    def test_update_work_order_status_records_completion(self):
        body = self.put_json("/work-orders/wo1/status?status=completed&notes=done")
        self.assertEqual(set(), WORK_ORDER_FIELDS - set(body["data"]))
        self.assertEqual("completed", body["data"]["status"])
        self.assertIsNotNone(body["data"]["completedAt"])
        self.assertIn("done", body["data"]["notes"])

    def test_update_work_order_status_rejects_invalid_status(self):
        resp = self.client.put(BASE + "/work-orders/wo1/status?status=bogus")
        self.assertEqual(resp.status_code, 400)

    def test_update_unknown_work_order_status_is_404(self):
        resp = self.client.put(BASE + "/work-orders/nope/status?status=completed")
        self.assertEqual(resp.status_code, 404)


class RouteCoverageTest(unittest.TestCase):
    def test_every_apiservice_path_is_routed(self):
        """Each Retrofit path in ApiService.kt must resolve to a Flask rule."""
        declared = {BASE + p for p in (
            "/users", "/pemanfaatan", "/announcements", "/messages",
            "/messages/<receiver_id>", "/community-posts",
            "/payments/initiate", "/payments/<payment_id>/status",
            "/payments/<payment_id>/confirm", "/vendors",
            "/vendors/<vendor_id>", "/work-orders",
            "/work-orders/<work_order_id>",
            "/work-orders/<work_order_id>/assign",
            "/work-orders/<work_order_id>/status",
        )}
        served = {r.rule for r in mock.app.url_map.iter_rules()
                  if r.rule.startswith(BASE)}
        missing = declared - served
        self.assertEqual(set(), missing, f"unrouted app endpoints: {sorted(missing)}")


if __name__ == "__main__":
    unittest.main(verbosity=2)
