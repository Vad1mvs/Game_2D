package states;

import static handlers.B2DVars.PPM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

import entities.Coin;
import entities.HUD;
import entities.Player;
import entities.Spike;
import handlers.B2DVars;
import handlers.VSContactListener;
import handlers.Input;
import handlers.Background;
import handlers.BoundedCamera;
import handlers.GameStateManager;
import main.Game;
import states.GameState;

public class Play extends GameState {
	
	private boolean debug = false;
	
	private World world;
	private Box2DDebugRenderer b2dRenderer;
	private VSContactListener cl;
	private BoundedCamera b2dCam;
	
	private Player player;
	
	private TiledMap tileMap;
	private int tileMapWidth;
	private int tileMapHeight;
	private int tileSize;
	private OrthogonalTiledMapRenderer tmRenderer;
	
	private Array<Coin> Coin;
	private Array<Spike> spikes;
	
	private Background[] backgrounds;
	private HUD hud;
	
	public static int level;
	
	public Play(GameStateManager gsm) {
		
		super(gsm);
		
		// set up the box2d world and contact listener
		world = new World(new Vector2(0, -7f), true);
		cl = new VSContactListener();
		world.setContactListener(cl);
		b2dRenderer = new Box2DDebugRenderer();
		
		// create player
		createPlayer();
		
		// create walls
		createWalls();
		cam.setBounds(0, tileMapWidth * tileSize, 0, tileMapHeight * tileSize);
		
		// create Coin
		createCoin();
		player.setTotalCoin(Coin.size);
		
		// create spikes
		createSpikes();
		
		// create backgrounds
		Texture bgs = Game.res.getTexture("bgs");
		TextureRegion sky = new TextureRegion(bgs, 0, 0, 750, 140);
		TextureRegion clouds = new TextureRegion(bgs, 0, 140, 750, 140);
		TextureRegion mountains = new TextureRegion(bgs, 0, 10, 1024, 240);
		backgrounds = new Background[3];
		backgrounds[0] = new Background(sky, cam, 0f);
		backgrounds[1] = new Background(clouds, cam, 0.1f);
		backgrounds[2] = new Background(mountains, cam, 0.2f);
		
		// create hud
		hud = new HUD(player);
		
		// set up box2d cam
		b2dCam = new BoundedCamera();
		b2dCam.setToOrtho(false, Game.V_WIDTH / PPM, Game.V_HEIGHT / PPM);
		b2dCam.setBounds(0, (tileMapWidth * tileSize) / PPM, 0, (tileMapHeight * tileSize) / PPM);
			}

	private void createPlayer() {
		
		// create bodydef
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DynamicBody;
		bdef.position.set(60 / PPM, 120 / PPM);
		bdef.fixedRotation = true;
		bdef.linearVelocity.set(1f, 0f);
		
		// create body from bodydef
		Body body = world.createBody(bdef);
		
		// create box shape for player collision box
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(13 / PPM, 13 / PPM);
		
		// create fixturedef for player collision box
		FixtureDef fdef = new FixtureDef();
		fdef.shape = shape;
		fdef.density = 1;
		fdef.friction = 0;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK | B2DVars.BIT_COIN | B2DVars.BIT_SPIKE;
		
		// create player collision box fixture
		body.createFixture(fdef);
		shape.dispose();
		
		// create box shape for player foot
		shape = new PolygonShape();
		shape.setAsBox(13 / PPM, 3 / PPM, new Vector2(0, -13 / PPM), 0);
		
		// create fixturedef for player foot
		fdef.shape = shape;
		fdef.isSensor = true;
		fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
		fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK;
		
		// create player foot fixture
		body.createFixture(fdef).setUserData("foot");;
		shape.dispose();
		
		// create new player
		player = new Player(body);
		body.setUserData(player);
		
		// final tweaks, manually set the player body mass to 1 kg
		MassData md = body.getMassData();
		md.mass = 1;
		body.setMassData(md);
		
		// i need a ratio of 0.005
		// so at 1kg, i need 200 N jump force
		
	}
	
	/**
	 * Sets up the tile map collidable tiles.
	 * Reads in tile map layers and sets up box2d bodies.
	 */
	private void createWalls() {
		
		// load tile map and map renderer
		try {
			tileMap = new TmxMapLoader().load("res/maps/level" + level + ".tmx");
		}
		catch(Exception e) {
			System.out.println("Cannot find file: res/maps/level" + level + ".tmx");
			Gdx.app.exit();
		}
		tileMapWidth = (int) tileMap.getProperties().get("width");
		tileMapHeight = (int) tileMap.getProperties().get("height");
		tileSize = (int) tileMap.getProperties().get("tilewidth");
		tmRenderer = new OrthogonalTiledMapRenderer(tileMap);
		
		// read each of the "red" "green" and "blue" layers
		TiledMapTileLayer layer;
		layer = (TiledMapTileLayer) tileMap.getLayers().get("red");
		createBlocks(layer, B2DVars.BIT_RED_BLOCK);
		layer = (TiledMapTileLayer) tileMap.getLayers().get("black");
		createBlocks(layer, B2DVars.BIT_BLACK_BLOCK);
		layer = (TiledMapTileLayer) tileMap.getLayers().get("blue");
		createBlocks(layer, B2DVars.BIT_BLUE_BLOCK);
		layer = (TiledMapTileLayer) tileMap.getLayers().get("yellow");
		createBlocks(layer, B2DVars.BIT_YELLOW_BLOCK);
		}
	private void createBlocks(TiledMapTileLayer layer, short bits) {
		
		// tile size
		float ts = layer.getTileWidth();
		
		// go through all cells in layer
		for(int row = 0; row < layer.getHeight(); row++) {
			for(int col = 0; col < layer.getWidth(); col++) {
				
				// get cell
				Cell cell = layer.getCell(col, row);
				
				// check that there is a cell
				if(cell == null) continue;
				if(cell.getTile() == null) continue;
				
				// create body from cell
				BodyDef bdef = new BodyDef();
				bdef.type = BodyType.StaticBody;
				bdef.position.set((col + 0.5f) * ts / PPM, (row + 0.5f) * ts / PPM);
				ChainShape cs = new ChainShape();
				Vector2[] v = new Vector2[3];
				v[0] = new Vector2(-ts / 2 / PPM, -ts / 2 / PPM);
				v[1] = new Vector2(-ts / 2 / PPM, ts / 2 / PPM);
				v[2] = new Vector2(ts / 2 / PPM, ts / 2 / PPM);
				cs.createChain(v);
				FixtureDef fd = new FixtureDef();
				fd.friction = 0;
				fd.shape = cs;
				fd.filter.categoryBits = bits;
				fd.filter.maskBits = B2DVars.BIT_PLAYER;
				world.createBody(bdef).createFixture(fd);
				cs.dispose();
				}
		}
	}
	private void createCoin() {
		
		// create list of Coin
		Coin = new Array<Coin>();
		
		// get all crystals in "Coin" layer,
		// create bodies for each, and add them
		// to the Coin list
		MapLayer ml = tileMap.getLayers().get("Coin");
		if(ml == null) return;
		
		for(MapObject mo : ml.getObjects()) {
			BodyDef cdef = new BodyDef();
			cdef.type = BodyType.StaticBody;
			float x = (float) mo.getProperties().get("x") / PPM;
			float y = (float) mo.getProperties().get("y") / PPM;
			cdef.position.set(x, y);
			Body body = world.createBody(cdef);
			FixtureDef cfdef = new FixtureDef();
			CircleShape cshape = new CircleShape();
			cshape.setRadius(8/ PPM);
			cfdef.shape = cshape;
			cfdef.isSensor = true;
			cfdef.filter.categoryBits = B2DVars.BIT_COIN;
			cfdef.filter.maskBits = B2DVars.BIT_PLAYER;
			body.createFixture(cfdef).setUserData("Coin");
			Coin c = new Coin(body);
			body.setUserData(c);
			Coin.add(c);
			cshape.dispose();
		}
	}
	
	/**
	 * Set up box2d bodies for spikes in "spikes" layer
	 */
	private void createSpikes() {
		
		spikes = new Array<Spike>();
		
		MapLayer ml = tileMap.getLayers().get("spikes");
		if(ml == null) return;
		
		for(MapObject mo : ml.getObjects()) {
			BodyDef cdef = new BodyDef();
			cdef.type = BodyType.StaticBody;
			float x = (float) mo.getProperties().get("x") / PPM;
			float y = (float) mo.getProperties().get("y") / PPM;
			cdef.position.set(x, y);
			Body body = world.createBody(cdef);
			FixtureDef cfdef = new FixtureDef();
			CircleShape cshape = new CircleShape();
			cshape.setRadius(5 / PPM);
			cfdef.shape = cshape;
			cfdef.isSensor = true;
			cfdef.filter.categoryBits = B2DVars.BIT_SPIKE;
			cfdef.filter.maskBits = B2DVars.BIT_PLAYER;
			body.createFixture(cfdef).setUserData("spike");
			Spike s = new Spike(body);
			body.setUserData(s);
			spikes.add(s);
			cshape.dispose();
		}
		
	}
	
	
	/**
	 * Apply upward force to player body.
	 */
	private void playerJump() {
		if(cl.playerCanJump()) {
			player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, 0);
			player.getBody().applyForceToCenter(0, 150, true);
			Game.res.getSound("jump").play();
		}}
		private void playerBigJump() {
		
		if(cl.playerCanJump()) {
			player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().x, 0);
			player.getBody().applyForceToCenter(0, 300, true);
			Game.res.getSound("jump").play();
		}
		}

	private void switchBlocks() {
		
		// get player foot mask bits
		Filter filter = player.getBody().getFixtureList().get(1).getFilterData();
		short bits = filter.maskBits;
		
		// switch to next block bit
		// red -> green -> blue
		if(bits == B2DVars.BIT_RED_BLOCK) {
			bits = B2DVars.BIT_BLACK_BLOCK;
		}
		else if(bits == B2DVars.BIT_BLACK_BLOCK) {
			bits = B2DVars.BIT_BLUE_BLOCK;
		}
		else if(bits == B2DVars.BIT_BLUE_BLOCK) {
			bits = B2DVars.BIT_YELLOW_BLOCK;
		}
		else if(bits == B2DVars.BIT_YELLOW_BLOCK) {
			bits = B2DVars.BIT_RED_BLOCK;
		}
		
		// set player foot mask bits
		filter.maskBits = bits;
		player.getBody().getFixtureList().get(1).setFilterData(filter);
		
		// set player mask bits
		bits |= B2DVars.BIT_COIN | B2DVars.BIT_SPIKE;
		filter.maskBits = bits;
		player.getBody().getFixtureList().get(0).setFilterData(filter);
		
		// play sound
		Game.res.getSound("changeblock").play();
		
	}
	
	public void handleInput() {
		
		// keyboard input
		if(Input.isPressed(Input.BUTTON1)) {
			playerJump();
		}
		
		if(Input.isPressed(Input.BUTTON3)) {
			playerBigJump();
		}
		
		if(Input.isPressed(Input.BUTTON2)) {
			switchBlocks();
		}
		
		
		//////////
		
		
		// mouse/touch input for android
		// left side of screen to switch blocks
		// right side of screen to jump
		if(Input.isPressed()) {
			if(Input.x < Gdx.graphics.getWidth() / 2) {
				switchBlocks();
			}
			else {
				playerJump();
			}
		}
		
	}
	
	public void update(float dt) {
		
		// check input
		handleInput();
		
		// update box2d world
		world.step(Game.STEP, 1, 1);
		
		// check for collected crystals
		Array<Body> bodies = cl.getBodies();
		for(int i = 0; i < bodies.size; i++) {
			Body b = bodies.get(i);
			Coin.removeValue((Coin) b.getUserData(), true);
			world.destroyBody(bodies.get(i));
			player.collectCoin();
			Game.res.getSound("crystal").play();
		}
		bodies.clear();
		
		// update player
		player.update(dt);
		
		// check player win
		if(player.getBody().getPosition().x * PPM > tileMapWidth * tileSize) {
			Game.res.getSound("levelselect").play();
			gsm.setState(GameStateManager.LEVEL_SELECT);
		}
		
		// check player failed
		if(player.getBody().getPosition().y < 0) {
			Game.res.getSound("hit").play();
			gsm.setState(GameStateManager.MENU);
		}
		if(player.getBody().getLinearVelocity().x < 0.001f) {
			Game.res.getSound("hit").play();
			gsm.setState(GameStateManager.MENU);
		}
		if(cl.isPlayerDead()) {
			Game.res.getSound("hit").play();
			gsm.setState(GameStateManager.MENU);
		}
		
		// update Coin
		for(int i = 0; i < Coin.size; i++) {
			Coin.get(i).update(dt);
		}
		
		// update spikes
		for(int i = 0; i < spikes.size; i++) {
			spikes.get(i).update(dt);
		}
		
	}
	
	public void render() {
		
		// camera follow player
		cam.setPosition(player.getPosition().x * PPM + Game.V_WIDTH / 4, Game.V_HEIGHT / 2);
		cam.update();
		
		// draw bgs
		sb.setProjectionMatrix(hudCam.combined);
		for(int i = 0; i < backgrounds.length; i++) {
			backgrounds[i].render(sb);
		}
		
		// draw tilemap
		tmRenderer.setView(cam);
		tmRenderer.render();
		
		// draw player
		sb.setProjectionMatrix(cam.combined);
		player.render(sb);
		
		// draw Coin
		for(int i = 0; i < Coin.size; i++) {
			Coin.get(i).render(sb);
		}
		
		// draw spikes
		for(int i = 0; i < spikes.size; i++) {
			spikes.get(i).render(sb);
		}
		
		// draw hud
		sb.setProjectionMatrix(hudCam.combined);
		hud.render(sb);
		
		// debug draw box2d
		if(debug) {
			b2dCam.setPosition(player.getPosition().x + Game.V_WIDTH / 4 / PPM, Game.V_HEIGHT / 2 / PPM);
			b2dCam.update();
			b2dRenderer.render(world, b2dCam.combined);
		}
		
	}
	
	public void dispose() {
		// everything is in the resource manager com.neet.blockbunny.handlers.Content
	}
	
}