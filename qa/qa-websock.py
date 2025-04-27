#!/bin/python
# test the echo websocket
# note: requires python3 virtual environment and pip install websockets
import unittest
import asyncio
import websockets
import os


class WebSocketTest(unittest.TestCase):
    @classmethod
    def setup_class(cls):
        script_dir = os.path.dirname(os.path.abspath(__file__))
        os.chdir(script_dir)

    async def connect_and_test_small_message(self):
        host = os.getenv("QA_HOST", "localhost")
        port = os.getenv("QA_PORT", "8888")
        uri = f"ws://{host}:{port}/b/test/ws"
        async with websockets.connect(uri) as websocket:
            message = "Hello, WebSocket ᐖᐛツ"
            await websocket.send(message)
            response = await websocket.recv()
            self.assertEqual(message, response)
            await websocket.close()

    async def connect_and_test_large_file(self):
        host = os.getenv("QA_HOST", "localhost")
        port = os.getenv("QA_PORT", "8888")
        uri = f"ws://{host}:{port}/b/test/ws"
        async with websockets.connect(uri) as websocket:
            with open("files/sample.txt", "r") as f:
                message = f.read()

            await websocket.send(message)
            response = await websocket.recv()
            self.assertEqual(message, response)
            await websocket.close()

    def test_websocket_echo(self):
        asyncio.run(self.connect_and_test_small_message())

    def test_websocket_large_file(self):
        asyncio.run(self.connect_and_test_large_file())


if __name__ == "__main__":
    unittest.main()
