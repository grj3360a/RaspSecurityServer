package me.security.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.security.hardware.sensors.Sensor;
import me.security.managers.DatabaseManager.Log;
import utils.JUnitUtil;

/**
 * @author Geraldes Jocelyn
 * @since 15/12/2019 Manage a web server to answer requests from mobile app
 */
public class RestAPIManager {

	public static int PORT = 8080;

	private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private static final List<String> AUTHS = Arrays.asList("eaz897hfg654kiu714sf32d1");

	private final ServerSecurity security;
	private final ServerSocket server;
	private boolean enabled;

	/**
	 * Immediately start a web server on PORT to answer queries from mobile app
	 * 
	 * @param security The main SecuManager
	 * @throws IOException If ServerSocket fails to create
	 */
	public RestAPIManager(ServerSecurity security) throws IOException, BindException {
		this.security = security;
		this.enabled = true;

		this.server = new ServerSocket(PORT);
		PORT = this.server.getLocalPort();
		System.out.println("Listening on port " + server.getLocalPort());

		new Thread(() -> {
			while (RestAPIManager.this.enabled) {
				try {
					ConnectionThread thread = new ConnectionThread(RestAPIManager.this.security, server.accept());
					thread.start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try {
				RestAPIManager.this.server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Manage a specific connection from a single socket
	 */
	private class ConnectionThread extends Thread {

		private final ServerSecurity security;
		private Socket client;

		public ConnectionThread(ServerSecurity security, Socket client) {
			this.security = security;
			this.client = client;
		}

		public void run() {
			InputStream input = null;
			OutputStream output = null;
			Scanner inputReader = null;

			try {
				input = this.client.getInputStream();
				output = this.client.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			try {
				inputReader = new Scanner(input);
				inputReader.useDelimiter("\n");
				if (!inputReader.hasNext()) {
					sendError(output, "Invalid header ?");
					return;// Ignore invalid request.
				}

				String url = inputReader.next().split(" ")[1];

				/*
				 * Collect every line of the inputReader a Map<String, String>
				 */
				String headerLine = null;
				Map<String, String> headers = new HashMap<String, String>();
				while (inputReader.hasNext()) {
					headerLine = inputReader.next();
					headerLine = headerLine.substring(0, headerLine.length() - 1);
					if (headerLine.contains(": ")) {
						headers.put(headerLine.split(": ")[0], headerLine.split(": ")[1]);
					} else {
						break;
					}
				}

				if (!isAuthed(headers)) {
					sendNotAuth(output);
					return;
				}

				switch (url) { // Switch between different endpoints

				case "/alarm":
					sendText(output, this.security.isEnabled() + "");
					break;

				case "/alarm/toggle":
					this.security.toggleAlarm("APP.");
					sendText(output, this.security.isEnabled() + "");
					break;

				case "/alarm/test":
					this.security.triggerAlarm("TEST", "Activation de l'alarme de test.");
					sendText(output, "true");
					break;

				case "/notify":
					List<Log> logs = this.security.getDb().getLast10Logs();
					sendText(output, GSON.toJson(logs));
					break;

				case "/sensors":
					sendText(output, GSON.toJson(this.security.getSensors()));
					break;

				default:
					if (url.startsWith("/sensor/")) {
						try {
							Sensor target = null;
							int id = Integer.parseInt(url.split("/")[2]);
							for (Sensor s : this.security.getSensors()) {
								if (s.getId() == id) {
									target = s;
								}
							}

							if (target == null) {
								throw new IllegalArgumentException(id + "");
							}

							switch (url.replaceFirst("/sensor/" + id, "")) {

							case "/toggle":
								target.toggle();
								sendText(output, target.isEnabled() + "");
								break;

							// Potentially add other endpoints to manage sensor ?

							default:
								sendNotFound(output, url);
								break;

							}
						} catch (NumberFormatException ex) {
							throw new Exception("Invalid sensor ID.");
						}
					} else {
						throw new IllegalAccessException("Unknown endpoint: " + url);
					}
					break;
				}

				inputReader.close();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				sendNotFound(output, e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				sendError(output, e.getMessage());
			} finally {
				if (inputReader != null)
					inputReader.close();

				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Verify if headers contains valid auth password
		 * 
		 * @param headers Headers sended by a request
		 * @return True if headers contains appPassword & a valid auth password.
		 */
		private boolean isAuthed(Map<String, String> headers) {
			for (Entry<String, String> headerLine : headers.entrySet()) {
				if (headerLine.getKey().equals("appPassword")) {
					return AUTHS.contains(headerLine.getValue());
				}
			}

			return false;
		}

		/**
		 * Send error 500 to output stream from the socket
		 * 
		 * @param msg The specific problem encountered
		 */
		private void sendError(OutputStream output, String msg) {
			PrintStream out = new PrintStream(output);
			out.println("HTTP/1.0 500 Internal Server Error");
			out.println("");
			out.println("" + msg);
			out.println("");
			out.flush();
		}

		/**
		 * Send error 401 to output stream from the socket
		 */
		private void sendNotAuth(OutputStream output) {
			PrintStream out = new PrintStream(output);
			out.println("HTTP/1.0 401 Not Authed Correctly");
			out.println("");
			out.flush();
		}

		/**
		 * Send error 404 Not Found to output stream from the socket
		 * 
		 * @param url What url was not found
		 */
		private void sendNotFound(OutputStream output, String url) {
			PrintStream out = new PrintStream(output);
			out.println("HTTP/1.0 404 Not Found");
			out.println("");
			out.println("NOT FOUND : " + url);
			out.println("");
			out.flush();
		}

		/**
		 * Send OK 200 to output stream from the socket
		 * 
		 * @param text Json to answer
		 */
		private void sendText(OutputStream output, String text) {
			PrintStream out = new PrintStream(output);
			out.println("HTTP/1.0 200 OK");
			out.println("Content-Type:application/json");
			out.println("");
			out.println(text);
			out.println("");
		}

	}

	public void close() {
		this.enabled = false;
	}
}