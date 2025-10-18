# Synchronization Strategy

This document outlines the strategy for keeping all clients in a collaborative session in sync with each other.

## 1. Core Problem

In a real-time collaborative editor, multiple users can make changes to the same document simultaneously. The core challenge is to ensure that all users see the same document state (eventual consistency) and that their changes are not lost.

There are two primary approaches to solving this problem:

1.  **Operational Transformation (OT)**: A complex algorithm where operations are transformed to account for concurrent changes. It's powerful but notoriously difficult to implement correctly.
2.  **Conflict-free Replicated Data Types (CRDTs)**: Data structures that are designed to be merged without conflicts. They are mathematically proven to converge, making them a more robust, if sometimes less intuitive, choice.
3.  **Server-Authoritative Model**: A simpler approach where the server is the single source of truth. All client operations are sent to the server, which applies them sequentially and broadcasts the new state to all clients.

## 2. Chosen Strategy for MVP: Server-Authoritative

For the initial version (MVP) of Underroot, we will implement a **simple server-authoritative model**.

### How It Works

1.  **Single Source of Truth**: The server maintains the canonical version of the document in memory.
2.  **Client Operations**: When a client makes a change (e.g., `INSERT_TEXT`, `DELETE_TEXT`), it sends the operation to the server. The client does *not* immediately apply the change to its local view.
3.  **Sequential Processing**: The server processes incoming operations one by one, in the order they are received, from a single queue. This eliminates concurrency issues on the server side.
4.  **State Broadcast**: After applying an operation, the server broadcasts an `UPDATE_DOCUMENT` message containing the *entire new document state* to all connected clients in that session.
5.  **Client Update**: Clients receive the `UPDATE_DOCUMENT` message and replace their local document view with the new state from the server.

### Trade-offs

#### Advantages:

*   **Simplicity**: This is by far the easiest model to implement and reason about. It avoids the complexities of OT and CRDTs.
*   **Guaranteed Consistency**: Because the server dictates the state, all clients are guaranteed to converge to the same view.
*   **Low Client-side Logic**: The client's role is simple: send edits and display the state received from the server.

#### Disadvantages:

*   **Requires Constant Connection**: Users cannot make changes while offline.
*   **High Latency Perception**: Because clients must wait for the server to acknowledge their change and broadcast it back, the UI might feel sluggish on high-latency connections. The user's own keystrokes won't appear until the server confirms them.
*   **Scalability Bottleneck**: The server can become a bottleneck as the number of users and documents grows.
*   **Inefficient Bandwidth Usage**: Sending the entire document on every change is inefficient. This is acceptable for small documents and our MVP, but it will not scale well.

## 3. Future Work: Migrating to a More Advanced Model

The server-authoritative model is a great starting point, but it has its limitations. The long-term goal should be to migrate to a more sophisticated approach.

### Roadmap

1.  **MVP**: Implement the server-authoritative model as described above.
2.  **V2 - Diffs instead of Full State**: The first optimization will be to change the `UPDATE_DOCUMENT` message to send a diff (a description of the change) instead of the full document state. This will significantly reduce bandwidth usage.
3.  **V3 - CRDTs**: For full offline support and better scalability, the project should eventually migrate to a CRDT-based model. This would be a major architectural change, but it would make the editor much more robust and powerful.

By starting with a simple, server-authoritative model, we can get a functional MVP up and running quickly, while laying the groundwork for these future improvements.
