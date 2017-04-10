package com.iaox.farmer;

import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.script.RandomEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import com.iaox.farmer.handlers.LoginEvent;
import com.iaox.farmer.tcp.Mule;

@ScriptManifest(author = "Iaox", info = "Farms for you", logo = "", name = "IaoxMulezz", version = 0.5)
public class MuleFarmer extends Script {

	private Mule mule;
	private LoginEvent loginEvent;
	public static boolean shouldTrade = true;
	private String username;
	private String password;

	@Override
	public void onStart() throws InterruptedException {
		username = getBot().getUsername();
		password = "lifebook3";
		LoginEvent loginEvent = new LoginEvent(username, password);
		getBot().addLoginListener(loginEvent);
		execute(loginEvent);
		mule = new Mule(this);
	}

	public void login() {
		log("lets login");
		LoginEvent loginEvent = new LoginEvent(username, password);
		getBot().addLoginListener(loginEvent);
		execute(loginEvent);
	}

	@Override
	public int onLoop() {
		log("loop");

		if (!mule.isRunning()) {

			// if the host is online, execute. If not, lets logout untill
			// online!
			if (mule.getConnection()) {
				log("we have a connection");
				if (!client.isLoggedIn()) {
					login();
				} else {
					log("starting thread");
					mule.start();
					new ConditionalSleep(10000) {

						@Override
						public boolean condition() throws InterruptedException {
							// TODO Auto-generated method stub
							return mule.getThread().isInterrupted();
						}

					}.sleep();
				}
			} else {
				log("host is not available, lets logout and sleep until host is available");
				if (client.isLoggedIn()) {
					logoutTab.logOut();
				}
			}
		} else {
			if (mule.getSlaveName() == null) {
				logoutTab.logOut();
			} else if (mule.getSlaveName() != null) {
				log("we shoudl trade");
				if (!client.isLoggedIn()) {
					sleeps(3000);
					login();
				} else {
					log("we are ready to trade");
					if (mule.getCoinsNeeded() > 0) {
						muleTradeWithdraw();
					} else {
						muleTradeDeposit();
					}
				}
			} else {
				logoutTab.logOut();
			}
		}

		return 600;
	}

	private void muleTradeDeposit() {
		log("lets trade:" + mule.getSlaveName());
		if (getWorlds().getCurrentWorld() == mule.getSlaveWorld()) {
			log("good world!!");
			if (!trade.isCurrentlyTrading()) {
				Player p = players.closest(player -> player.getName().equals(mule.getSlaveName()));
				if (p != null) {
					log("player exists, lets trade");
					p.interact("Trade with");
					new ConditionalSleep(10000) {
						@Override
						public boolean condition() throws InterruptedException {
							return trade.isCurrentlyTrading();
						}

					}.sleep();
				}
			} else if (trade.isFirstInterfaceOpen()) {
				if (!trade.getTheirOffers().contains(995)) {
					log("waiting for him to put up cash");
				} else {
					trade.acceptTrade();
					new ConditionalSleep(10000) {

						@Override
						public boolean condition() throws InterruptedException {
							return trade.isSecondInterfaceOpen();
						}

					}.sleep();
				}
			} else if (trade.isSecondInterfaceOpen()) {
				trade.acceptTrade();
				new ConditionalSleep(10000) {
					@Override
					public boolean condition() throws InterruptedException {
						return trade.isCurrentlyTrading();
					}

				}.sleep();
			}
		} else {
			log(worlds.getCurrentWorld());
			log(mule.getSlaveWorld());
			worlds.hop(mule.getSlaveWorld());
		}

	}

	private void muleTradeWithdraw() {
		log("lets trade:" + mule.getSlaveName());
		if (getWorlds().getCurrentWorld() == mule.getSlaveWorld()) {
			log("good world!!");
			if (!trade.isCurrentlyTrading()) {
				Player p = players.closest(player -> player.getName().equals(mule.getSlaveName()));
				if (p != null) {
					log("player exists, lets trade");
					p.interact("Trade with");
					new ConditionalSleep(10000) {
						@Override
						public boolean condition() throws InterruptedException {
							return trade.isCurrentlyTrading();
						}

					}.sleep();
				}
			} else if (trade.isFirstInterfaceOpen()) {
				if (!trade.getOurOffers().contains(995)) {
					trade.offer(995, mule.getCoinsNeeded());
				} else {
					trade.acceptTrade();
					new ConditionalSleep(10000) {

						@Override
						public boolean condition() throws InterruptedException {
							return trade.isSecondInterfaceOpen();
						}

					}.sleep();
				}
			} else if (trade.isSecondInterfaceOpen()) {
				trade.acceptTrade();
				new ConditionalSleep(10000) {
					@Override
					public boolean condition() throws InterruptedException {
						return trade.isCurrentlyTrading();
					}

				}.sleep();
			}
		} else {
			log(worlds.getCurrentWorld());
			log(mule.getSlaveWorld());
			worlds.hop(mule.getSlaveWorld());
		}

	}

	public void sleeps(int milli) {
		try {
			sleep(milli);
		} catch (Exception e) {

		}
	}

	@Override
	public void onExit() throws InterruptedException {
		if (mule.getThread() != null) {
			mule.getThread().interrupt();
		}
	}

}
