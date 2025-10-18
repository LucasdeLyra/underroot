# Underroot Protocol Specification

This document defines the WebSocket protocol for communication between the Underroot client and server. All messages are serialized as JSON.

## Base Message Structure

Every message exchanged between the client and server is a JSON object containing a `type` field, which determines the message's purpose, and a `payload` field, which is a JSON object containing the data for that message type.

```json
{
  "type": "MESSAGE_TYPE_NAME",
  "payload": { ... }
}
```

---

## 1. Client-to-Server Messages

These messages are initiated by the client and sent to the server.

### `JOIN_DOCUMENT`

-   **Direction**: Client → Server
-   **Description**: Sent when a client wishes to join a collaborative session for a specific document. This is the first message a client should send after establishing a connection.
-   **Payload**:
    -   `docId` (string): The unique identifier for the document.
    -   `username` (string): A display name for the user joining.

-   **Example**:
    ```json
    {
      "type": "JOIN_DOCUMENT",
      "payload": {
        "docId": "sample-doc-123",
        "username": "Alice"
      }
    }
    ```

### `INSERT_TEXT`

-   **Direction**: Client → Server
-   **Description**: Sent when the user inserts text into the document.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `position` (number): The linear index where the insertion starts.
    -   `text` (string): The text to be inserted.

-   **Example**:
    ```json
    {
      "type": "INSERT_TEXT",
      "payload": {
        "docId": "sample-doc-123",
        "position": 42,
        "text": "new text"
      }
    }
    ```

### `DELETE_TEXT`

-   **Direction**: Client → Server
-   **Description**: Sent when the user deletes text from the document.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `position` (number): The linear index where the deletion starts.
    -   `length` (number): The number of characters to delete.

-   **Example**:
    ```json
    {
      "type": "DELETE_TEXT",
      "payload": {
        "docId": "sample-doc-123",
        "position": 42,
        "length": 8
      }
    }
    ```

### `REQUEST_COMPILE`

-   **Direction**: Client → Server
-   **Description**: Sent when a client requests a compilation of the current document state.
-   **Payload**:
    -   `docId` (string): The document identifier.

-   **Example**:
    ```json
    {
      "type": "REQUEST_COMPILE",
      "payload": {
        "docId": "sample-doc-123"
      }
    }
    ```

---

## 2. Server-to-Client Messages

These messages are initiated by the server and sent to one or more clients.

### `DOCUMENT_STATE`

-   **Direction**: Server → Client (unicast)
-   **Description**: Sent to a newly joined client to provide the full current state of the document.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `content` (string): The full text content of the document.
    -   `users` (array of strings): A list of usernames currently in the session.

-   **Example**:
    ```json
    {
      "type": "DOCUMENT_STATE",
      "payload": {
        "docId": "sample-doc-123",
        "content": "The entire document content...",
        "users": ["Alice", "Bob"]
      }
    }
    ```

### `UPDATE_DOCUMENT`

-   **Direction**: Server → Client (broadcast)
-   **Description**: Broadcast to all clients in a session when the document's content has changed. For a simple implementation, this can send the entire document content. A more advanced implementation would send a diff/patch.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `content` (string): The new, full text content of the document.

-   **Example**:
    ```json
    {
      "type": "UPDATE_DOCUMENT",
      "payload": {
        "docId": "sample-doc-123",
        "content": "The new, updated document content..."
      }
    }
    ```

### `USER_JOINED`

-   **Direction**: Server → Client (broadcast)
-   **Description**: Broadcast to all clients in a session (except the one who just joined) when a new user joins.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `username` (string): The display name of the user who joined.

-   **Example**:
    ```json
    {
      "type": "USER_JOINED",
      "payload": {
        "docId": "sample-doc-123",
        "username": "Charlie"
      }
    }
    ```

### `USER_LEFT`

-   **Direction**: Server → Client (broadcast)
-   **Description**: Broadcast to all clients in a session when a user disconnects.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `username` (string): The display name of the user who left.

-   **Example**:
    ```json
    {
      "type": "USER_LEFT",
      "payload": {
        "docId": "sample-doc-123",
        "username": "Alice"
      }
    }
    ```

### `COMPILE_RESULT`

-   **Direction**: Server → Client (unicast)
-   **Description**: Sent to the client who requested a compile, containing the result.
-   **Payload**:
    -   `docId` (string): The document identifier.
    -   `success` (boolean): `true` if the compilation was successful, `false` otherwise.
    -   `outputUrl` (string, optional): If successful, a URL to the compiled PDF.
    -   `logs` (string, optional): If failed, the error logs from the compiler.

-   **Example (Success)**:
    ```json
    {
      "type": "COMPILE_RESULT",
      "payload": {
        "docId": "sample-doc-123",
        "success": true,
        "outputUrl": "/downloads/sample-doc-123.pdf"
      }
    }
    ```

-   **Example (Failure)**:
    ```json
    {
      "type": "COMPILE_RESULT",
      "payload": {
        "docId": "sample-doc-123",
        "success": false,
        "logs": "Error on line 42: undefined control sequence..."
      }
    }
    ```
