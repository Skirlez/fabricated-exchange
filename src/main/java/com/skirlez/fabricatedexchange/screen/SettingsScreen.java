package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;



import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SettingsScreen extends GameOptionsScreen {
	private Map<String, Object> CONFIG;
	private final Map<String, String[]> COMMENTS;
	
	private final List<List<ClickableWidget>> pages;
	private int startY;
	private int lastY;
	private int distFromLeft;
	private int distFromRight;
	private int distFromBottom;
	private int currentPage;
	private long prevRenderTime;
	
	private static final ButtonWidget.PressAction NO_ACTION = new ButtonWidget.PressAction() {
		public void onPress(ButtonWidget widget) {	
		}
	};
	
	private final List<Ball> balls;
	
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public SettingsScreen(Screen previous) {
		super(previous, CLIENT.options, Text.of("Fabricated Exchange"));
		CONFIG = ModDataFiles.MAIN_CONFIG_FILE.getCopy();
		COMMENTS = ModDataFiles.MAIN_CONFIG_FILE.getComments();
		pages = new ArrayList<List<ClickableWidget>>();
		currentPage = 0;
		prevRenderTime = System.nanoTime();
		
		balls = new ArrayList<Ball>();
		
	}
	

	private static Item choose(Item... items) {
		return items[new Random().nextInt(items.length)];
	}
	

	@Override
	protected void init() {
		
		this.startY = 35;
		super.init();
		if (this.client.world != null && !MinecraftClient.getInstance().isIntegratedServerRunning()) {
			// multiplayer
			List<Text> warningTexts = GeneralUtil.translatableList("screen.fabricated-exchange.settings.client_warning");
			for (Text text : warningTexts) {
				PressableTextWidget serverWarningWidget = new PressableTextWidget(this.width / 2, startY, 0, 0, text, NO_ACTION, textRenderer);
				//serverWarningWidget.setTextColor(MathHelper.packRgb(1, 0, 0));
				addDrawable(serverWarningWidget);
				this.startY += 10;
			}

		}

	
		this.lastY = this.startY;
		this.distFromLeft = 30;
		this.distFromRight = width - 30;
		this.distFromBottom = 30;
		this.balls.clear();
		
		balls.add(Ball.randomVelocityBall(ModItems.PHILOSOPHERS_STONE, this.width / 2 + 50 + java.lang.Math.random() * 50d - 25d, this.height / 2 + java.lang.Math.random() * 50d - 25d));
		for (int i = 0; i < 7; i++) 
			balls.add(Ball.randomVelocityBall(choose(ModItems.DARK_MATTER, ModItems.RED_MATTER), this.width / 2 + 50 + java.lang.Math.random() * 50d - 25d, this.height / 2 + java.lang.Math.random() * 50d - 25d));
		
		this.pages.clear();
		pages.add(new ArrayList<ClickableWidget>());
		Text resetToDefaultText = Text.translatable("screen.fabricated-exchange.settings.reset");
		ButtonWidget resetToDefault = new ButtonWidget.Builder(resetToDefaultText, (widget) -> {
			CONFIG = ModDataFiles.MAIN_CONFIG_FILE.copyDefaultValue();
			clearAndInit();
		}).dimensions(distFromLeft, height - distFromBottom, textRenderer.getWidth(resetToDefaultText) + 20, 20).build();
		
		addDrawableChild(resetToDefault);
		
		Text closeWithoutSavingText = Text.translatable("screen.fabricated-exchange.settings.close");
		ButtonWidget closeWithoutSaving = new ButtonWidget.Builder(closeWithoutSavingText, (widget) -> {
			super.close();
		}).dimensions(distFromLeft + resetToDefault.getWidth() + 11, height - distFromBottom, textRenderer.getWidth(closeWithoutSavingText) + 20, 20).build();
		
		addDrawableChild(closeWithoutSaving);
		
		Text doneText = Text.translatable("gui.done");
		ButtonWidget done = new ButtonWidget.Builder(doneText, (widget) -> {
			ModDataFiles.MAIN_CONFIG_FILE.setValueAndSave(CONFIG);
			super.close();
		}).dimensions(distFromLeft + resetToDefault.getWidth() + closeWithoutSaving.getWidth() + 22, height - distFromBottom, closeWithoutSaving.getWidth(), 20).build();
		
		addDrawableChild(done);
		

		addBooleanValue("showItemEmcOrigin");
		addBooleanValue("showEnchantedBookRepairCost");
		addSuperNumberValue("enchantmentEmcConstant");
		addSuperNumberValue("emcInMultiplier");
		addSuperNumberValue("emcOutMultiplier");
		addBooleanValue("mapper.enabled");
		addBooleanValue("transmutationTable.animated");
		addBooleanValue("transmutationTable.floorButton");
		addBooleanValue("antiMatterRelay.onlyAcceptFuelItems");
		addBooleanValue("energyCollector.alwaysHaveEnergy");
		
		if (pages.get(pages.size() - 1).isEmpty())
			pages.remove(pages.size() - 1);
		
		final PressableTextWidget pageTextWidget = new PressableTextWidget(
				distFromRight - 40, this.height / 2 - distFromBottom + 1, 
				13, 13, Text.of(Integer.toString(currentPage)), NO_ACTION, textRenderer);
		
		if (pages.size() > 1) {

			addDrawable(pageTextWidget);
			
			addDrawableChild(ButtonWidget.builder(
				Text.of("<"), button -> switchPage(currentPage - 1, pageTextWidget))
				.dimensions(distFromRight - 60, this.height / 2 - distFromBottom, 13, 13)
				.build());
	
			addDrawableChild(ButtonWidget.builder(
				Text.of(">"), button -> switchPage(currentPage + 1, pageTextWidget))
				.dimensions(distFromRight - 20, this.height / 2 - distFromBottom, 13, 13)
				.build());
			
				
		}
		// No widgets are active yet, open currentPage which is 0 or whatever it was before pressing reset to default
		switchPage(currentPage, pageTextWidget);
	}

	private void switchPage(int page, PressableTextWidget pageTextWidget) {
		if ((page <= -1) || (page >= pages.size()))
			return;
		for (ClickableWidget widget : pages.get(currentPage)) {
			widget.active = false;
			widget.visible = false;
		}
		currentPage = page;
		for (ClickableWidget widget : pages.get(currentPage)) {
			widget.active = true;
			widget.visible = true;
		}
		pageTextWidget.setMessage(Text.of(Integer.toString(currentPage)));
	}
	
	@Override
	public void removed() {
		
	}
	
	private void addSuperNumberValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		

		
		
		Text nameText = Text.of(key + ":");
		int width = textRenderer.getWidth(nameText);
		int height = 12;
		PressableTextWidget nameWidget = new PressableTextWidget(distFromLeft, lastY + height / 2, width, height, nameText, NO_ACTION, textRenderer);
		nameWidget.setTooltip(TooltipCom.of());
		nameWidget.active = false;
		nameWidget.visible = false;
		addDrawableChild(nameWidget);
		pages.get(pages.size() - 1).add(nameWidget);
	

		
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, distFromLeft + width + 10, lastY + height / 2 - 1, 50, height, Text.empty());

		Text invalidText = Text.of("Invalid number!!");
		int invalidWidth = textRenderer.getWidth(invalidText);
		TextWidget invalidTextWidget = new TextWidget(
				distFromLeft + nameWidget.getWidth() + field.getWidth() + 20, 
				lastY + height / 2, invalidWidth, height, 
				Text.empty(), textRenderer);
		
		addDrawable(invalidTextWidget);
		
		pages.get(pages.size() - 1).add(invalidTextWidget);
		
		field.setChangedListener((str) -> {
			boolean isNumberValid = SuperNumber.isValidNumberString(str);
			invalidTextWidget.setMessage(isNumberValid ? Text.empty() : invalidText);
			if (isNumberValid)
				CONFIG.put(key, str);
		});
		field.setText((String)CONFIG.get(key));
		field.active = false;
		field.visible = false;
		addDrawableChild(field);
		pages.get(pages.size() - 1).add(field);
		
		increaseY();
	}
	
	// this is set up to only be called when closing with esc, so we should save here
	@Override
	public void close() {
		ModDataFiles.MAIN_CONFIG_FILE.setValueAndSave(CONFIG);
		super.close();
	}
	
	private void addBooleanValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		
		Text f = Text.of(key + ": OFF");
		Text t = Text.of(key + ": ON");
		int width = textRenderer.getWidth(f) + 15;
		//TextWidget value = new TextWidget(distFromLeft + width + 20, lastY + 10, 0, 0, (boolean)CONFIG.get(key) ? t : f, textRenderer);
		//addDrawable(value);
		ButtonWidget button = new ButtonWidget.Builder((boolean)CONFIG.get(key) ? t : f, (widget) -> {
			boolean newValue = !(boolean)CONFIG.get(key);
			CONFIG.put(key, newValue);
			widget.setMessage(newValue ? t : f);
		}).dimensions(distFromLeft, lastY, width, 20).tooltip(Tooltip.of(commentsText)).build();
		button.active = false;
		button.visible = false;
		
		addDrawableChild(button);
		pages.get(pages.size() - 1).add(button);
		
		increaseY();
	}
	
	private void increaseY() {
		lastY += 25;
		if (height - 60 < lastY) {
			lastY = startY;
			pages.add(new ArrayList<ClickableWidget>());
		}
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		
		super.render(matrices, mouseX, mouseY, delta);
		itemRenderer.renderGuiItemIcon(new ItemStack(ModItems.DARK_MATTER_HOE), mouseX - 8, mouseY - 8);
		GameOptionsScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, this.title.asOrderedText(), this.width / 2, 20, 0xFFFFFF);
		drawBalls(matrices, mouseX, mouseY);

	}
	
	private void drawBalls(MatrixStack matrices, int mouseX, int mouseY) {
		long currentTime = System.nanoTime();
		
		double dotX = this.width / 2 + 50;
		double dotY = this.height / 2; //+ Math.sin(currentTime / 1000000000.0) * 10;
		
		double dt = (currentTime - prevRenderTime) / 10000000.0;
		for (Ball ball : balls) {
			
			
			// accelerate towards point
			ball.addVelocity(new Vector2d(
					dotX - ball.pos.x, 
					dotY - ball.pos.y)
					.div(2000d)
					.mul(java.lang.Math.random() / 2 + 1)
					.mul(dt));
			
	
			
			
			// move away from mouse
			double dx = (ball.pos.x - mouseX);
			double dy = (ball.pos.y - mouseY);
			double distToMouse = java.lang.Math.sqrt(dx * dx + dy * dy);
			double maxDist = 40d;
			if (distToMouse < maxDist) {
				ball.addVelocity(new Vector2d(
						mouseX - ball.pos.x, 
						mouseY - ball.pos.y)
						.div(1000d)
						.mul((maxDist - distToMouse) / maxDist * 40d)
						.mul(dt)
						.negate());
			}
			
			
			ball.tick(dt);
			// bounce
			if (ball.pos.x < 8) {
				ball.pos.x = 8;
				if (ball.vel.x < 0)
					ball.vel.x *= -1;
			}
			else if (ball.pos.x > width - 8) {
				ball.pos.x = width - 8;
				if (ball.vel.x > 0)
					ball.vel.x *= -1;
			}
			if (ball.pos.y < 8) {
				ball.pos.y = 8;
				if (ball.vel.y < 0)
					ball.vel.y *= -1;
			}
			else if (ball.pos.y > height - 8) {
				ball.pos.y = height - 8;
				if (ball.vel.y > 0)
					ball.vel.y *= -1;
			}
			
			
			itemRenderer.renderGuiItemIcon(new ItemStack(ball.item), (int)ball.pos.x - 8, (int)ball.pos.y - 8);
		}
		prevRenderTime = currentTime;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Too many
		
		//if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
		//	balls.add(Ball.randomVelocityBall(choose(ModItems.PHILOSOPHERS_STONE, ModItems.DARK_MATTER, ModItems.RED_MATTER), mouseX, mouseY));
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private static class Ball {
		private Vector2d pos;
		private Vector2d vel;
		private Item item;
		
		
		public Ball(Item item, Vector2d pos, Vector2d vel) {
			this.item = item;
			this.pos = pos;
			this.vel = vel;	
		}
		
		public static Ball randomVelocityBall(Item item, double x, double y) {
			return new Ball(item,
					new Vector2d(x, y), 
					new Vector2d(java.lang.Math.random() * 2d - 1d, java.lang.Math.random() * 2d - 1d));
		}


		public void addVelocity(Vector2d vel) {
			this.vel.add(vel);
		}
		
		public void tick(double dt) {
			if (vel.magnitude() > 1d)
				vel.fma(-0.025 * dt, vel.normalize());
			pos.add(new Vector2d(vel).mul(dt));
			
		}
	}
	
	private static class Vector2d {
		public double x;
		public double y;
		public Vector2d(double x, double y) {
			this.x = x;
			this.y = y;
		}
		public Vector2d(Vector2d other) {
			this.x = other.x;
			this.y = other.y;
		}
		public double magnitude() {
			return Math.sqrt(x * x + y * y);
		}

		public Vector2d normalize() {
			double mag = magnitude();
			if (mag == 0)
				return new Vector2d(0, 0);
			x /= mag;
			y /= mag;
			return this;
		}
		public Vector2d add(Vector2d other) {
			x += other.x;
			y += other.y;
			return this;
		}
		public Vector2d div(double scalar) {
			x /= scalar;
			y /= scalar;
			return this;
		}

		public Vector2d mul(double scalar) {
			x *= scalar;
			y *= scalar;
			return this;
		}
		public Vector2d fma(double scalar, Vector2d other) {
			x += scalar * other.x;
			y += scalar * other.y;
			return this;
		}
		public Vector2d negate() {
			x *= -1;
			y *= -1;
			return this;
		}
	}
}
