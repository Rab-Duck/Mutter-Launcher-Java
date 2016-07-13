package com.rabduck.mutter;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.event.ActionEvent;

import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController implements Initializable{

	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainController.class.getName());;

	@FXML
	private ComboBox<String> cmbbxSearchText;
	@FXML
	private TextField txtPathView;
	@FXML
	private ListView<Item> itemListView;
	@FXML
	private Button buttonOk;
	@FXML
	private Button buttonCancel;
	@FXML
	private Button btnUpdate;
	@FXML
	private Button btnExit;

	private ObservableList<Item> items = FXCollections.observableArrayList();

		
	private CollectorService collectorService = new CollectorService();
	private MainCollector collector;
	
	private void updateView(String searchStr){
		if(searchStr == null){
			searchStr = cmbbxSearchText.getEditor().getText();
		}
		if(collector != null){
			items.clear();
			items.addAll(collector.grep(searchStr));
			itemListView.getSelectionModel().select(0);
			btnUpdate.setDisable(false);
		}
	}
	
    class CollectorService extends ScheduledService<MainCollector> {
        @Override
        protected MainCollector createTask() {
            return new MainCollector();
        }
    }
    
	private void collect(){
		// collector = new MainCollector();
		if(collectorService != null){
			collectorService.restart();
//		    Thread thread = new Thread(collector);
//		    thread.setDaemon(true);
//		    thread.start();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.log(Level.FINE, "Controller Initialized!" + location + ", " + resources );
		
		itemListView.setItems(items);

		// collector = new MainCollector();
		collectorService.setDelay(Duration.ZERO);
		collectorService.setPeriod(new Duration(1000*60*60*6));
		collectorService.setOnSucceeded(value -> {
			logger.log(Level.FINE, "collect thread is succeeded:" + value);
			collector = collectorService.getValue();
			updateView(null);
		});
		collectorService.setOnFailed(value -> {
			logger.log(Level.FINE, "collect thread is failed:" + value);
		});
		collectorService.setOnScheduled(value -> {
			logger.log(Level.FINE, "collect thread is scheduled:" + value);
		});
		collectorService.setOnRunning(value -> {
			btnUpdate.setDisable(true);
		});
		collect();
		// collector.getAllItemList().stream().forEach(item -> {logger.log(item.getItemName() + ":" + item.getItemPath());});

		// reference:
		// java - JavaFX - ComboBox listener for its texfield - Stack Overflow
		// http://stackoverflow.com/questions/18657317/javafx-combobox-listener-for-its-texfield
		cmbbxSearchText.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
		    logger.log(Level.FINE, "cmbbx changed from " + oldValue + " to " + newValue);
			updateView(newValue);
		});
		cmbbxSearchText.getEditor().setOnKeyPressed((event) -> {
			logger.log(Level.FINER, "onKeyPressed:" + event);
			OnKeyPressedCommon(event);
		});
		cmbbxSearchText.getEditor().setOnKeyTyped((event) -> {
			logger.log(Level.FINE, "onKeyTyped:" + event);
			if(event.getCode() == KeyCode.ENTER || event.getCharacter().equals("\n") || event.getCharacter().equals("\r")){
				executeSelectedItem();
			}
		});
		cmbbxSearchText.getEditor().setOnAction((event) -> {
			logger.log(Level.FINEST, "OnAction editorProperty:" + event);
			// executeSelectedItem();
		});
		cmbbxSearchText.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String t, String t1) {
				 logger.log(Level.FINEST, ov.toString()); logger.log(Level.FINEST,t); logger.log(Level.FINEST,t1);
			}
		});
		
		// txtPathView.textProperty().bind(itemList.getSelectionModel().selectedItemProperty());
		itemListView.getSelectionModel().selectedItemProperty().addListener(listener -> {
			txtPathView.textProperty().set( 
					itemListView.getSelectionModel().getSelectedIndex() < 0 ?
							"" :
								itemListView.getSelectionModel().selectedItemProperty().get().getItemPath());
		});
		
		// unused by NullPointerException 
		// primaryStage = (Stage)buttonCancel.getScene().getWindow();
	}

	// Event Listener on CombBox[#cmbbxSearchText].onAction
	@FXML
	public void actionCmbbxSearchText(ActionEvent event) {
		logger.log(Level.FINEST, "actionCmbbxSearchText:" + event);
	}
	@FXML
	public void onKeyPressedCmbbxSearchText(KeyEvent event){
		logger.log(Level.FINEST, "onKeyPressedCmbbxSearchText:" + event);
	}
	@FXML
	public void onKeyTypedCmbbxSearchText(KeyEvent event){
		logger.log(Level.FINEST, "onKeyTypedCmbbxSearchText:" + event);
	}
	
	private void OnKeyPressedCommon(KeyEvent event){

		switch (event.getCode()) {
		case DOWN:
		case UP:
		case PAGE_UP:
		case PAGE_DOWN:
		// case HOME:
		// case END:
			itemListView.fireEvent(event);
			event.consume();
			break;
		default:
			break;
		}
	}
	
	public void onKeyTypedItemListView(KeyEvent event){
		logger.log(Level.FINER, "onKeyTypedItemListView:" + event);
		if(event.getCode() == KeyCode.ENTER || event.getCharacter().equals("\n") || event.getCharacter().equals("\r")){
			executeSelectedItem();
		}
	}
	
	// Event Listener on Button[#buttonOk].onAction
	@FXML
	public void onActionOk(ActionEvent event) {
		executeSelectedItem();
	}
	
	private void executeSelectedItem(){
		int selectedIndex = itemListView.getSelectionModel().getSelectedIndex();
		logger.log(Level.FINE, "selectedIndex:" + selectedIndex);
		if(selectedIndex >= 0){
			try {
				items.get(selectedIndex).execute("");
			} catch (ExecException e) {
				e.printStackTrace();
			}
			stage.hide();
			cmbbxSearchText.setValue("");
		}
	}
	
	// Event Listener on Button[#buttonCancel].onAction
	@FXML
	public void onActionCancel(ActionEvent event) {
		// buttonCancel.getScene().getWindow().hide();
		if(stage != null){ stage.hide(); }
	}

	// Event Listener on Button[#btnUpdate].onAction
	@FXML
	public void onActionUpdate(ActionEvent event) {
		collect();
	}

	// Event Listener on Button[#btnExit].onAction
	@FXML
	public void onActionExit(ActionEvent event) {
		Platform.exit();
	}

	private Stage stage;
	public void setStage(Stage stage){
		this.stage = stage;
	}
}
