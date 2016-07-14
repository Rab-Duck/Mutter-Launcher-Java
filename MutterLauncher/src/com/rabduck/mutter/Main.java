package com.rabduck.mutter;

	
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;


import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class Main extends Application {

	private static Logger logger;
	
	private TrayIcon icon;
	private Stage primaryStage;
	private EnvManager envmngr = EnvManager.getInstance();
	
	
	@Override
	public void init() throws Exception {
		logger.log(Level.INFO, "Call Main::init!");
		super.init();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		logger.log(Level.INFO, "Call Main::stop!");
		// for killing awt thread
		SystemTray.getSystemTray().remove(icon);
		JIntellitype.getInstance().cleanUp();
	}

	@Override
	public void start(Stage primaryStage) {
		logger.log(Level.INFO, "Call Main::start!");
		this.primaryStage = primaryStage;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			Pane root = (Pane)loader.load();
			
			// reference:
			// JavaFX: How to get stage from controller during initialization? - Stack Overflow
			// http://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
			MainController controller = (MainController)loader.getController();
			controller.setStage(primaryStage);

			Scene scene = new Scene(root, envmngr.getIntProperty("MainWinWidth"),envmngr.getIntProperty("MainWinHeight"));
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setOnCloseRequest((WindowEvent t) -> {
				primaryStage.hide();
	            t.consume();
	        });
			primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				if(event.getCode() == KeyCode.ESCAPE){
					primaryStage.hide();
				}
			});
			
			primaryStage.setScene(scene);
			primaryStage.setTitle("Mutter Launcher@Java");
			primaryStage.initStyle(StageStyle.UTILITY);
			primaryStage.show();
			
			// reference:
			// awt - JavaFX app in System Tray - Stack Overflow
			// http://stackoverflow.com/questions/12571329/javafx-app-in-system-tray
			Platform.setImplicitExit(false);
		    SystemTray tray = SystemTray.getSystemTray();

		    // アイコンの生成と登録
		    icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream("reload_12x14.png")));
	        icon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runLaterShow();
				}
			});
	        icon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if(e.getButton() == MouseEvent.BUTTON1){
						runLaterShow();
					}
				}
			});
		    tray.add(icon);

		    // register Windows Hot Key
		    JIntellitype.getInstance().registerHotKey(1, envmngr.getIntProperty("HotkeyMod"), envmngr.getIntProperty("HotkeyKey"));
			JIntellitype.getInstance().addHotKeyListener(new HotkeyListener(){
				@Override
				public void onHotKey(int arg0) {
					runLaterShow();
				}
			});
			
		} catch(Exception e) {
			ErrorDialog.showErrorDialog("Application failed to initialize", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void runLaterShow(){
		Platform.runLater(() -> {
			primaryStage.show();
			primaryStage.toFront();
		});		
	}
	
	public static void main(String[] args) {
		try(InputStream in = Main.class.getClassLoader().getResourceAsStream("com/rabduck/mutter/logging.properties")){
			LogManager.getLogManager().readConfiguration(in);
		}catch (IOException e){
			e.printStackTrace();
		}
		logger = Logger.getLogger(com.rabduck.mutter.Main.class.getName());
		launch(args);
	}
}
