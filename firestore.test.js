const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

test("users can read and write resident profiles", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("profiles").doc("user1").set({
      auth_user_id: "user1",
      full_name: "Alex Kimani",
      email: "alex@capitalhome.co.ke",
      role: "resident",
      room_id: "room_204",
    })
  );
  await assertSucceeds(db.collection("profiles").doc("user1").get());
});

test("users can create and read rooms", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("rooms").doc("room_204").set({
      room_number: "204",
      room_type: "New Room",
      status: "Occupied",
      resident_id: "user1",
    })
  );
  await assertSucceeds(db.collection("rooms").doc("room_204").get());
});

test("users can create and read marketplace products", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("products").doc("prod1").set({
      seller_id: "seller1",
      name: "Wireless Earbuds",
      price: 1500,
      status: "Active",
    })
  );
  await assertSucceeds(db.collection("products").doc("prod1").get());
});

test("users can create and read community posts", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("posts").doc("post1").set({
      author_id: "user1",
      author_name: "Alex",
      content: "Welcome to Capital Home Residence!",
    })
  );
  await assertSucceeds(db.collection("posts").doc("post1").get());
});

test("users can create and read chat messages", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("messages").doc("msg1").set({
      conversation_id: "conv1",
      sender_id: "user1",
      content: "Hello neighbor",
    })
  );
  await assertSucceeds(db.collection("messages").doc("msg1").get());
});

test("users cannot delete profiles", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await db.collection("profiles").doc("user1").set({ full_name: "Alex" });
  await assertFails(db.collection("profiles").doc("user1").delete());
});
