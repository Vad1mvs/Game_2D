package handlers;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import handlers.Input;

public class InputProcessor extends InputAdapter {
		
		public boolean mouseMoved(int x, int y) {
			Input.x = x;
			Input.y = y;
			return true;
		}
		
		public boolean touchDragged(int x, int y, int pointer) {
			Input.x = x;
			Input.y = y;
			Input.down = true;
			return true;
		}
		
		public boolean touchDown(int x, int y, int pointer, int button) {
			Input.x = x;
			Input.y = y;
			Input.down = true;
			return true;
		}
		
		public boolean touchUp(int x, int y, int pointer, int button) {
			Input.x = x;
			Input.y = y;
			Input.down = false;
			return true;
		}
		
		public boolean keyDown(int k) {
			if(k == Keys.Z) Input.setKey(Input.BUTTON1, true);
			if(k == Keys.X) Input.setKey(Input.BUTTON2, true);
			if(k == Keys.A) Input.setKey(Input.BUTTON3, true); 
			return true;
		}
		
		public boolean keyUp(int k) {
			if(k == Keys.Z) Input.setKey(Input.BUTTON1, false);
			if(k == Keys.X) Input.setKey(Input.BUTTON2, false); 
			if(k == Keys.A) Input.setKey(Input.BUTTON3, false); 
			return true;
		}
		
	}

