package com.iaox.farmer.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import org.osbot.rs07.script.Script;

import com.iaox.farmer.MuleFarmer;

public class Mule implements Runnable {
	String hostName = "0.0.0.0";
	int port = 44444;
	String message = "";
	private String name;
	private Script script;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	int i = 0;
	private String type;

	private String mule;

	private Thread t;
	
	private String slave;
	private int slaveWorld;
	private String threadName;
	private int coinsNeeded;

	public Mule(Script script) {
		this.script = script;
		this.threadName = slave + " " + mule;
	}

	public boolean getConnection() {
		try {
			clientSocket = new Socket(hostName, port);
			i = 0;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isConnected() {
		return clientSocket != null && !clientSocket.isClosed();
	}

	private String getName() {
		if (name != null) {
			return name;
		} else if (this.script.myPlayer().getName() != null) {
			name = script.myPlayer().getName();
			return name;
		}
		return Protocol.BREAK;
	}

	public String getSlaveName() {
		return slave;
	}
	
	public int getSlaveWorld(){
		return slaveWorld;
	}

	public String getMule() {
		if (mule != null) {
			return mule;
		}
		return null;
	}

	public Thread getThread() {
		return t;
	}

	public void killConnection() {
		if (isConnected()) {
			try {
				out.println(Protocol.BREAK);
				clientSocket.shutdownInput();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

	public boolean isRunning() {
		return t != null && t.isAlive();
	}

	@Override
	public void run() {
		script.log("we are running");
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			i = 0;
			
			message = in.readLine();
			script.log(message);
			while (!message.equals(Protocol.BREAK) && clientSocket != null && out != null && in != null) {
				script.log("tcp loop");
				if (message.startsWith("Mule:")) {
					mule = message.substring(5);
					script.log("our mule is:" + mule);
				}
				if (message.startsWith("Slave:")) {
					slave = message.substring(6);
					script.log("our slave is:" + slave);
				}
				if (message.startsWith("World:")){
					script.log("world message: "  + message);
					slaveWorld = Integer.parseInt(message.substring(6));
					script.log("our world is:" + getSlaveWorld());
				}
				if (message.startsWith("Coins:")){
					script.log("money needed: "  + message);
					coinsNeeded = Integer.parseInt(message.substring(6));
					script.log("money needed" + coinsNeeded);
				}
				switch (message) {
				case Protocol.TRADE_DONE:
					script.log("we are done with the trade!");
					slave = null;
					out.println(Protocol.TRUE);
					break;
				case Protocol.ASK_FOR_ACCOUNT_TYPE:
					script.log("enter your account type (mule3 or slave4)");
					type = Protocol.MULE;
					out.println(type);
					break;
				case Protocol.ASK_FOR_NAME:
					script.log("enter your account name");
					out.println(getName());
					break;
				case Protocol.ASK_FOR_ASSIGNMENT:
					script.log("enter your assignment wish");
					out.println(Protocol.WITHDRAW);
					break;
				case Protocol.ASK_FOR_AMOUNT_OF_CASH:
					script.log("enter the amount of cash");
					out.println(3000);
				case Protocol.ASK_IF_SHOULD_RUN:
					// if trade is done, return BREAK
					if (MuleFarmer.shouldTrade) {
						out.println(Protocol.TRUE);
					} else {
						if (type.equals(Protocol.SLAVE)) {
							out.println(Protocol.BREAK);
							clientSocket.close();
						} else {
							message = in.readLine();
						}
					}
					i++;
					break;
				default:
					out.println(Protocol.TRUE);
					break;
				}
				message = in.readLine();
				System.out.println(message);
			}
			clientSocket.close();
			System.out.println("Connection has been closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.interrupt();
		script.log("we exit");
	}

	public int getCoinsNeeded() {
		return coinsNeeded;
	}
}
