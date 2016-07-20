package com.rabduck.mutter;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;

import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class MainController implements Initializable{

	private static Logger logger = Logger.getLogger(com.rabduck.mutter.MainController.class.getName());
	private static EnvManager envmngr;
	private static final boolean bUseJLabel = false;

	@FXML
	private ComboBox<String> cmbbxSearchText;
	@FXML
	private TextField txtPathView;
	@FXML
	private ListView<Item> itemListView;
	@FXML
	private Button btnExec;
	@FXML
	private Button btnClose;
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
		if(collectorService != null){
			collectorService.restart();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.log(Level.INFO, "Controller Initialized!" + location + ", " + resources );

		try {
			envmngr = EnvManager.getInstance();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Env file I/O error:", e);
			e.printStackTrace();
			ErrorDialog.showErrorDialog("Env file I/O error:", e, false);
			System.exit(-1);
		}

		itemListView.setItems(items);
		
		itemListView.setCellFactory(new Callback<ListView<Item>, ListCell<Item>>() {
		     @Override
		     public ListCell<Item> call(ListView<Item> list) {
		         return new ItemFormatCell();
		     }
		 });

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
		
		// txtPathView.textProperty().bind(itemList.getSelectionModel().selectedItemProperty());
		itemListView.getSelectionModel().selectedItemProperty().addListener(listener -> {
			txtPathView.textProperty().set( 
					itemListView.getSelectionModel().getSelectedIndex() < 0 ?
							"" :
								itemListView.getSelectionModel().selectedItemProperty().get().getItemPath());
		});
		
		collector = new MainCollector();
		if(collector.cachedCollect()){
			updateView("");
		}
		else{
			collector = null;
		}

		collectorService.setDelay(Duration.ZERO);
		collectorService.setPeriod(new Duration(1000*60*envmngr.getIntProperty("ResearchInterval")));
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
		// collector.getAllItemList().stream().forEach(item -> {logger.log(Level.FINEST, item.getItemName() + ":" + item.getItemPath());});
		
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
	
	// onMouseClicked="#onMouseClickedListView"
	public void onMouseClickedListView(MouseEvent event){
		logger.log(Level.FINER, "onMouseClickedListView:" + event);
		if(event.getClickCount() >= 2){
			executeSelectedItem();
		}
	}
	
	// Event Listener on Button[#buttonOk].onAction
	@FXML
	public void onActionExec(ActionEvent event) {
		executeSelectedItem();
	}
	
	private void executeSelectedItem(){
		int selectedIndex = itemListView.getSelectionModel().getSelectedIndex();
		logger.log(Level.FINE, "selectedIndex:" + selectedIndex);
		if(selectedIndex >= 0){
			try {
				items.get(selectedIndex).execute("");
				collector.setExecHistory(items.get(selectedIndex));
				stage.hide();
				cmbbxSearchText.setValue("");
				updateView("");
			} catch (ExecException e) {
				e.printStackTrace();
				ErrorDialog.showErrorDialog("Execute Error:", e, false);
			}
		}
	}
	
	// Event Listener on Button[#buttonCancel].onAction
	@FXML
	public void onActionClose(ActionEvent event) {
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

	final class ItemFormatCell extends ListCell<Item> {
	    public ItemFormatCell() {    }
	      
	    @Override protected void updateItem(Item item, boolean empty) {
	    	
	        super.updateItem(item, empty);
	        
	        if(empty || item == null){
	        	 setText(null);
	        	 setGraphic(null);
	        	 return;
	        }
	        
	        setText(item.getItemName());
	        setContentDisplay(ContentDisplay.LEFT);

	        if(bUseJLabel){
		        SwingNode sn = new SwingNode();
		        SwingUtilities.invokeLater(() -> {
		        	sn.setContent(new JLabel(item.getIcon()));
		        });
		        setGraphic(sn);
	        }
	        else{
	            // reference: 
				// 	JavaFX file listview with icon and file name - Stack Overflow
				// 	http://stackoverflow.com/questions/28034432/javafx-file-listview-with-icon-and-file-name        	
	            Icon icon = item.getIcon();
	            BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
	            icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
	            setGraphic(new ImageView(SwingFXUtils.toFXImage(bufferedImage, null)));        	
	        }
	    }
	}
}

