package com.rabduck.mutter;

	
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.imageio.ImageIO;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;


import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class Main extends Application {
	private TrayIcon icon;
	private Stage primaryStage;
	@Override
	public void init() throws Exception {
		System.out.println("Call Main::init!");
		super.init();
	}

	@Override
	public void stop() throws Exception {
		System.out.println("Call Main::stop!");
		// for kill awt thread
		SystemTray.getSystemTray().remove(icon);
		JIntellitype.getInstance().cleanUp();
		super.stop();
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			Pane root = (Pane)loader.load();
			
			// reference:
			// JavaFX: How to get stage from controller during initialization? - Stack Overflow
			// http://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
			MainController controller = (MainController)loader.getController();
			controller.setStage(primaryStage);

			Scene scene = new Scene(root,422,386);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setOnCloseRequest((WindowEvent t) -> {
				primaryStage.hide();
	            t.consume();
	        });
			primaryStage.setScene(scene);
			primaryStage.setTitle("Mutter Launcher@Java");
			primaryStage.initStyle(StageStyle.UTILITY);
			// primaryStage.show();
			
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
		    JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_WIN, (int)'C');
			JIntellitype.getInstance().addHotKeyListener(new HotkeyListener(){
				@Override
				public void onHotKey(int arg0) {
					runLaterShow();
				}
			});
			
		} catch(Exception e) {
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
		launch(args);
	}
}
