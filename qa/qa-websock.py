# note: requires python3 virtual environment and pip install websockets
import unittest
import asyncio
import websockets


class WebSocketTest(unittest.TestCase):
    async def connect_and_test(self):
        uri = "ws://localhost:8888/b/test/ws"
        async with websockets.connect(uri) as websocket:
            message = "Hello, WebSocket ᐖᐛツ"
            await websocket.send(message)
            response = await websocket.recv()
            self.assertEqual(message, response)

    def test_websocket_echo(self):
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        loop.run_until_complete(self.connect_and_test())


if __name__ == "__main__":
    unittest.main()
