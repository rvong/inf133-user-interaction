package a.multitouch;

import org.mt4j.MTApplication;

public class StartBubbleGame extends MTApplication {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		initialize();
	}
	
	@Override
	public void startUp() {
		addScene(new BubbleGameScene(this, "Bubble Game"));
	}
}

