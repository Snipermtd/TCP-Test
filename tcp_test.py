import socket
import os
import time
import tkinter as tk
from tkinter import filedialog

def select_file():
    root = tk.Tk()
    root.withdraw()  # Hide the main Tkinter window
    file_path = filedialog.askopenfilename(
        title="Select a File",
        filetypes=[("All files", "*.*")]
    )
    if file_path:
        print(f"Selected file: {file_path}")
        # You can now open and process the file using file_path
    else:
        print("No file selected.")
    return file_path

def send_file_to_android(file_path: str):
    host = '127.0.0.1'
    port = 8080

    print(f"Attempting to connect to {host}:{port}...")

    filesize = os.path.getsize(file_path)
    filename = os.path.basename(file_path)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((host, port))

        # Send header
        header = f"{filename}:{filesize}\n".encode()
        s.sendall(header)

        # Send file contents in chunks
        with open(file_path, "rb") as f:
            while True:
                chunk = f.read(4096)
                if not chunk:
                    break
                s.sendall(chunk)

    print("File sent successfully.")
                
    
    print("File sent successfully")

def send_string_to_android(message: str):
    """
    Connects to a TCP server on localhost and sends a string.
    """
    host = '127.0.0.1'  # This is always localhost for adb forward
    port = 8080         # PC port that was forwarded

    print(f"Attempting to connect to {host}:{port}...")

    try:
        # Create a socket object. The with statement ensures it's closed automatically.
        # SOCK_STREAM = TCP socket
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            # Set a timeout so the script doesn't hang forever if it can't connect.
            s.timeout(5)
            
            # Attempt to connect to the server.
            s.connect((host, port))
            print(" Connection successful!")

            #End of message. The server looks for a new line to know when the message ends.
            message_with_newline = message + '\n'
            
            # Send the data. Has to be encoded into bytes.
            s.sendall(message_with_newline.encode('utf-8'))
            print(f"Sent: '{message}'")
            time.sleep(0.1)
            print(" Data sent successfully.")

    except socket.timeout:
        print(" Error: Connection timed out.")
    except ConnectionRefusedError:
        print(" Error: Connection refused.")
    except Exception as e:
        print(f" An unexpected error occurred: {e}")


if __name__ == "__main__":
    #send_string_to_android("Hello from YOUR PC")
    send_file_to_android(select_file())