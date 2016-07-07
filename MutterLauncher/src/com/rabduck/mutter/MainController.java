package com.rabduck.mutter;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class MainController implements Initializable{
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
	private Button btnExit;

	private ObservableList<Item> items;

		
	private MainCollector collector;
	
	private void updateView(String searchStr){
		if(searchStr == null){
			searchStr = cmbbxSearchText.getValue();
		}
		items.clear();
		items.addAll(collector.grep(searchStr));
		itemListView.getSelectionModel().select(0);
	}
	
	private void collect(){
		if(collector != null){
			collector.setOnSucceeded(value -> {
				System.out.println("collect thread is succeeded:" + value);
				updateView(null);
			});
			collector.setOnFailed(value -> {
				System.out.println("collect thread is failed:" + value);
			});
		    Thread thread = new Thread(collector);
		    thread.setDaemon(true);
		    thread.start();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("Controller Initialized!" + location + ", " + resources );
		
		collector = new MainCollector();
		collect();
		// collector.getAllItemList().stream().forEach(item -> {System.out.println(item.getItemName() + ":" + item.getItemPath());});
		items = FXCollections.observableArrayList();
		itemListView.setItems(items);

		// reference:
		// java - JavaFX - ComboBox listener for its texfield - Stack Overflow
		// http://stackoverflow.com/questions/18657317/javafx-combobox-listener-for-its-texfield
		cmbbxSearchText.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
		    System.out.println("cmbbx changed from " + oldValue + " to " + newValue);
			updateView(newValue);
		});
		cmbbxSearchText.getEditor().setOnKeyPressed((event) -> {
			System.out.println("onKeyPressed:" + event);
			OnKeyPressedCommon(event);
		});
		cmbbxSearchText.getEditor().setOnKeyTyped((event) -> {
			System.out.println("onKeyTyped:" + event);
			if(event.getCode() == KeyCode.ENTER || event.getCharacter().equals("\n") || event.getCharacter().equals("\r")){
				executeSelectedItem();
			}
		});
		cmbbxSearchText.getEditor().setOnAction((event) -> {
			System.out.println("OnAction editorProperty:" + event);
			// executeSelectedItem();
		});
		cmbbxSearchText.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String t, String t1) {
				 System.out.println(ov); System.out.println(t); System.out.println(t1);
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
		System.out.println("actionCmbbxSearchText:" + event);
	}
	@FXML
	public void onKeyPressedCmbbxSearchText(KeyEvent event){
		System.out.println("onKeyPressedCmbbxSearchText:" + event);
	}
	@FXML
	public void onKeyTypedCmbbxSearchText(KeyEvent event){
		System.out.println("onKeyTypedCmbbxSearchText:" + event);
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
		System.out.println("onKeyTypedItemListView:" + event);
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
		System.out.println("selectedIndex:" + selectedIndex);
		if(selectedIndex >= 0){
			try {
				items.get(selectedIndex).execute("");
			} catch (ExecException e) {
				e.printStackTrace();
			}
		}
		stage.hide();
		cmbbxSearchText.setValue("");
	}
	
	// Event Listener on Button[#buttonCancel].onAction
	@FXML
	public void onActionCancel(ActionEvent event) {
		// buttonCancel.getScene().getWindow().hide();
		if(stage != null){ stage.hide(); }
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
