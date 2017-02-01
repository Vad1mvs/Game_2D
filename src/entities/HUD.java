package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import entities.Player;
import handlers.B2DVars;
import main.Game;

public class HUD {
private Player player;
	
	private TextureRegion container;
	private TextureRegion[] blocks;
	private TextureRegion Coin;
	private TextureRegion[] font;
	
	public HUD(Player player) {
		
		this.player = player;
		
		Texture tex = Game.res.getTexture("hud");
		
		container = new TextureRegion(tex, 0, 0, 32, 32);
		
		blocks = new TextureRegion[4];
		for(int i = 0; i < blocks.length; i++) {
			blocks[i] = new TextureRegion(tex, 32 + i * 16, 0, 16, 16);
		}
		
		Coin = new TextureRegion(tex, 80, 45, 16, 16);
		
		font = new TextureRegion[11];
		for(int i = 0; i < 6; i++) {
			font[i] = new TextureRegion(tex, 32 + i * 9, 16, 9, 9);
		}
		for(int i = 0; i < 5; i++) {
			font[i + 6] = new TextureRegion(tex, 32 + i * 9, 25, 9, 9);
		}
		
	}
	
	public void render(SpriteBatch sb) {
		
		sb.begin();
		
		// draw container
		sb.draw(container, 32, 200);
		
		// draw blocks
		short bits = player.getBody().getFixtureList().first().getFilterData().maskBits;
		if((bits & B2DVars.BIT_RED_BLOCK) != 0) {
			sb.draw(blocks[0], 40, 208);
		}
		else if((bits & B2DVars.BIT_BLACK_BLOCK) != 0) {
			sb.draw(blocks[1], 40, 208);
		}
		else if((bits & B2DVars.BIT_BLUE_BLOCK) != 0) {
			sb.draw(blocks[2], 40, 208);
		}
		else if((bits & B2DVars.BIT_YELLOW_BLOCK) != 0) {
			sb.draw(blocks[3], 40, 208);
		}
		
		// draw Coin
		sb.draw(Coin, 100, 208);
		
		// draw Coin amount
		drawString(sb, player.getNumCoin() + " / " + player.getTotalCoin(), 132, 211);
		
		sb.end();
		
	}
	
	private void drawString(SpriteBatch sb, String s, float x, float y) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == '/') c = 10;
			else if(c >= '0' && c <= '9') c -= '0';
			else continue;
			sb.draw(font[c], x + i * 9, y);
		}
	}
	
}

