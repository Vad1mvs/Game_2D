package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import entities.B2DSprite;
import main.Game;

public class Player extends B2DSprite {
	
	private int numCoin;
	private int totalCoin;
	
	public Player(Body body) {
		
		super(body);
		
		Texture tex = Game.res.getTexture("Cossacks");
		TextureRegion[] sprites = new TextureRegion[4];
		for(int i = 0; i < sprites.length; i++) {
			sprites[i] = new TextureRegion(tex, i * 32, 0, 32, 32);
		}
		
		animation.setFrames(sprites, 1 / 12f);
		
		width = sprites[0].getRegionWidth();
		height = sprites[0].getRegionHeight();
		
	}
	
	public void collectCoin() { numCoin++; }
	public int getNumCoin() { return numCoin; }
	
	public void setTotalCoin(int i) { totalCoin = i; }
	public int getTotalCoin() { return totalCoin; }
	
}
