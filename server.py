import http.server
import socketserver
import os

PORT = 5689
DIRECTORY = "/home/phuc/hoc-cnpm"

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

if __name__ == "__main__":
    with socketserver.TCPServer(("0.0.0.0", PORT), Handler) as httpd:
        print(f"Serving CNPM Review Web on port {PORT}")
        httpd.serve_forever()
