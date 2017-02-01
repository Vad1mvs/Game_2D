package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import entities.B2DSprite;
import main.Game;

public class Coin extends B2DSprite {
	
	public Coin(Body body) {
		
		super(body);
		
		Texture tex = Game.res.getTexture("Coin");
		TextureRegion[] sprites = TextureRegion.split(tex, 15, 16)[0];
		animation.setFrames(sprites, 1 / 30f);
		
		width = sprites[0].getRegionWidth();
		height = sprites[0].getRegionHeight();
		
	}
	
}

