package org.rpi.mplayer;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.player.IPlayer;
import org.rpi.player.IPlayerEventClassListener;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventUpdateTrackMetaData;
import org.rpi.playlist.CustomTrack;

public class MPlayer implements IPlayer {

	private Logger log = Logger.getLogger(MPlayer.class);
	private List<IPlayerEventClassListener> _listeners = new ArrayList<IPlayerEventClassListener>();
	private Process process = null;
	private OutputReader reader = null;
	private InputWriter writer = null;
	private PositionThread position = null;

	private boolean bPaused = false;
	private boolean bPlaying = false;

	// private boolean bPreLoad = false;
	private boolean bLoading = false;

	private TrackInfo trackInfo = null;
	private long volume = 100;

	private String uniqueId = "";

	private CustomTrack current_track = null;
	private boolean bMute;

	/***
	 * Plays the Custom Track
	 * 
	 * @param track
	 * @return
	 */
	public boolean playTrack(CustomTrack track, long volume, boolean mute) {
		uniqueId = track.getUniqueId();
		this.volume = volume;
		this.bMute = mute;
		current_track = track;
		log.info("Starting to playTrack Id: " + uniqueId);
		String url = track.getUri();
		try {
			initProcess(url);
		} catch (Exception e) {
			log.error("Error playTrack: ", e);
		}
		return true;
	}

	/***
	 * PreLoad the Track ready for when the current Track ends
	 */
	public void preLoadTrack(CustomTrack track) {
	}

	/***
	 * Used to start a pre loaded track
	 */
	public void startTrack() {
		startPlaying();
	}

	/***
	 * If the Player is Playing we can change tracks.
	 * 
	 * @param t
	 */
	public void openFile(CustomTrack t) {
	}

	public synchronized void startPlaying() {
		log.debug("Starting to Play: " + uniqueId);
		setVolume(volume);
		if (bMute) {
			setMute(bMute);
		}
		log.debug("Started to Play: " + uniqueId);
		setStatus("Playing");
		setPlaying(true);
		bLoading = false;
		bPaused = false;
		log.debug("PositionThreadState: " + position.getState());
		if (position.getState() == State.NEW) {
			log.debug("Position Thread is in State NEW Start Thread");
			position.start();
		}
	}

	public synchronized void loaded() {
	}

	public synchronized void playingTrack() {
		startPlaying();
	}

	/***
	 * Attempt to Stop if already running If Position Thread is running,
	 * interrupt it
	 */
	private void init() {
		stop();
		if (position != null) {
			position.interrupt();
			position = null;
		} else {
			log.debug("Position Thread was null");
		}
	}

	/***
	 * Build the string to start the process
	 * 
	 * @param url
	 * @throws IOException
	 */
	private void initProcess(String url) throws IOException {
		try {
			List<String> params = new ArrayList<String>();
			params.add(Config.mplayer_path);
			params.add("-slave");
			params.add("-quiet");
			if (Config.mplayer_cache > 0) {
				params.add("-cache");
				params.add("" + Config.mplayer_cache);
			}
			if (Config.mplayer_cache_min > 0) {
				params.add("-cache-min");
				params.add("" + Config.mplayer_cache_min);
			}

			trackInfo = new TrackInfo(this);
			if (isPlayList(url)) {
				params.add("-playlist");
			}
			params.add(url);
			ProcessBuilder builder = new ProcessBuilder(params);
			builder.redirectErrorStream(true);
			process = builder.start();
			log.debug("Create new InputWriter");
			writer = new InputWriter(process);
			log.debug("Create new OutputReader");
			reader = new OutputReader(this);
			log.debug("Create new Position Thread");
			position = new PositionThread(this);
			log.debug("Create new Position Thread");
			position.setNewTrack(true);
			reader.start();
		} catch (Exception e) {
			log.error("Error initProcess: ", e);
		}
	}

	/***
	 * Is this one of the playlists we have configured
	 * 
	 * @param url
	 * @return
	 */
	private boolean isPlayList(String url) {
		for (String s : Config.playlists) {
			if (url.toLowerCase().contains(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void pause(boolean bPause) {
		bPaused = true;
		log.debug("Sending: pause");
		sendCommand("pause");

	}

	@Override
	public synchronized void stop() {
		log.debug("Sending: quit");
		sendCommand("quit");
	}

	@Override
	public synchronized void destroy() {
		log.debug("Attempting to Stop MPlayer");
		sendCommand("quit");
		reader = null;
		writer = null;
		if (position != null) {
			position.interrupt();
			position = null;
		}
	}

	public synchronized InputWriter getCommandWriter() {
		return writer;
	}

	/**
	 * @return the bPaused
	 */
	public boolean isbPaused() {
		return bPaused;
	}

	/**
	 * @return the bPlaying
	 */
	public boolean isPlaying() {
		if (process != null) {
			if (position != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bPlaying
	 *            the bPlaying to set
	 */
	public void setPlaying(boolean bPlaying) {
		log.debug("setPlaying: " + bPlaying);
		this.bPlaying = bPlaying;
	}

	/***
	 * Track has stopped Playing, get Next Track..
	 */
	public synchronized void stoppedPlaying() {
		writer.setStopSendingCommands(true);
		log.debug("Stopped Playing get next track: ");
		setPlaying(false);
		setStatus("Stopped");
		position.interrupt();
		position = null;
		reader = null;
		EventFinishedCurrentTrack ev = new EventFinishedCurrentTrack(this);
		fireEvent(ev);
	}

	/***
	 * Update OpenHome with the new Status
	 * 
	 * @param status
	 */
	public synchronized void setStatus(String status) {
		EventStatusChanged ev = new EventStatusChanged(this);
		ev.setStatus(status);
		ev.setTrack(current_track);
		fireEvent(ev);
	}

	public synchronized Process getProcess() {
		return this.process;
	}

	/**
	 * @return the trackInfo
	 */
	public synchronized TrackInfo getTrackInfo() {
		return trackInfo;
	}

	@Override
	public void setMute(boolean mute) {
		if (mute) {
			sendCommand("pausing_keep mute");
			// Bug Fix for MPlayer on Raspberry
			// sendCommand("pausing_keep volume 0 1");
		} else {
			sendCommand("pausing_keep mute");
			// Bug fix for MPlayer on Raspberry
			sendCommand("pausing_keep volume " + (volume - 1) + " 1");
			setVolume(volume);
		}
	}

	@Override
	public void setVolume(long volume) {
		this.volume = volume;
		sendCommand("pausing_keep volume " + volume + " 1");
	}

	@Override
	public void seekAbsolute(long seconds) {
		sendCommand("seek " + seconds + " 2");
	}

	/***
	 * Used by the ICY info to update the track being played on the Radio
	 * 
	 * @param artist
	 * @param title
	 */
	public synchronized void updateInfo(String artist, String title) {
		EventUpdateTrackMetaData ev = new EventUpdateTrackMetaData(this);
		ev.setArtist(artist);
		ev.setTitle(title);
		fireEvent(ev);
	}

	/***
	 * 
	 */
	public void endPositionThread() {
		if (position != null) {
			try {
				position.interrupt();
			} catch (Exception e) {
				log.error("Error Stopping Position Thread", e);
			}
		}
	}

	/***
	 * 
	 */
	@Override
	public synchronized void resume() {
		bPaused = false;
		sendCommand("pause");
	}

	/***
	 * Send Command to MPlayer
	 * 
	 * @param command
	 */
	public synchronized void sendCommand(String command) {
		if (writer != null) {
			log.debug("Sending: " + command + " TrackId: " + uniqueId);
			writer.sendCommand(command);
			log.debug("Sent: " + command + " TrackId: " + uniqueId);
		} else {
			log.info("Could Not Send Command, Writer was null: " + command);
		}
	}

	public synchronized void addEventListener(IPlayerEventClassListener listener) {
		_listeners.add(listener);
	}

	public synchronized void removeEventListener(IPlayerEventClassListener listener) {
		_listeners.remove(listener);
	}

	public synchronized void fireEvent(EventObject ev) {
		for (IPlayerEventClassListener l : _listeners) {
			l.handleMyEventClassEvent(ev);
		}
	}

	public boolean isLoading() {
		return bLoading;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UniqueId: " + uniqueId);
		sb.append("Writer: " + writer.toString());
		sb.append("Reader: " + reader.toString());
		return sb.toString();
	}

}