package uk.ac.lkl.client.composer;

import java.util.ArrayList;
import java.util.List;

import uk.ac.lkl.client.BehaviourComposer;
import uk.ac.lkl.client.ButtonWithDebugID;
import uk.ac.lkl.client.Modeller;
import uk.ac.lkl.client.RichTextEntry;
import uk.ac.lkl.client.PopupPanelWithKeyboardShortcuts;
import uk.ac.lkl.client.Utils;
import uk.ac.lkl.client.event.ActivateMacroBehaviourEvent;
import uk.ac.lkl.client.event.InactivateMacroBehaviourEvent;
import uk.ac.lkl.client.event.RemoveMacroBehaviourEvent;
import uk.ac.lkl.client.event.SwapPrototypesEvent;
import uk.ac.lkl.shared.CommonUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;

public class MacroBehaviourName extends ButtonWithDebugID {
    
    protected MacroBehaviourView macroBehaviourView;
    protected boolean fullPrototype;
    private String currentHTML;
    
    public MacroBehaviourName(String html, MacroBehaviourView macroBehaviourView, boolean fullPrototype) {
	super(html);
	this.currentHTML = html;
	this.macroBehaviourView = macroBehaviourView;
	this.fullPrototype = fullPrototype;
	setStylePrimaryName("modeller-prototype-name");
	addClickHandler(new ClickHandler() {

	    public void onClick(ClickEvent event) {
		if (BehaviourComposer.microBehaviourWaitingToBeAdded != null) {
		    MacroBehaviourName.this.macroBehaviourView.acceptOrWaitMicroBehaviourWaitingToBeAdded(0);
		} else {
		    createPopupMenu(MacroBehaviourName.this.macroBehaviourView,
			            getAbsoluteLeft()+getOffsetWidth()/2, 
			            getAbsoluteTop()+getOffsetHeight(),
			            MacroBehaviourName.this.fullPrototype,
			            event);
		}
	    }
	    
	});
	MouseOverHandler mouseOverHandler = new MouseOverHandler() {

	    @Override
	    public void onMouseOver(MouseOverEvent event) {
		if (BehaviourComposer.microBehaviourWaitingToBeAdded == null) {
		    setTitle(Modeller.constants.clickOnThisForMoreOptions());
		} else {
		    String description = BehaviourComposer.microBehaviourWaitingToBeAdded.getText();
		    setTitle(Modeller.constants.clickOnThisToAdd() + " " + description);
		}		
	    }
	     
	};
	addMouseOverHandler(mouseOverHandler);
    }
    
    protected void createPopupMenu(final MacroBehaviourView macroBehaviourView, 
	                           int menuX, int menuY, 
	                           boolean fullPrototype,
	                           ClickEvent popupClickEvent) {
	final PopupPanelWithKeyboardShortcuts popupMenu = new PopupPanelWithKeyboardShortcuts(true);
	MenuBar menu = new MenuBarWithDebugID(true);
	menu.setAnimationEnabled(true);
	popupMenu.setWidget(menu);
	popupMenu.setAnimationEnabled(true);
	MenuItem firstMenuItem = null;
	MenuItem menuItem;
	if (macroBehaviourView.showMicroBehaviours()) {
	    if (fullPrototype) {
		Command renameCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			final RichTextEntry richText = new RichTextEntry(MacroBehaviourName.this, null, MacroBehaviourName.this.getOffsetHeight()*2);
			MacroBehaviourName.this.setVisible(false);
			richText.addSaveButtonClickHandler(new ClickHandler() {
			    @Override
			    public void onClick(ClickEvent event) {
				macroBehaviourView.acceptHTML(richText.getRichTextArea());
				richText.removeFromParent();
				MacroBehaviourName.this.setVisible(true);
			    }
			});
			richText.addCancelButtonClickHandler(new ClickHandler() {
			    @Override
			    public void onClick(ClickEvent event) {
				richText.removeFromParent();
				MacroBehaviourName.this.setVisible(true);
				macroBehaviourView.macroBehaviourChanged();
			    }
			});
			SimplePanel panel = new SimplePanel();
			panel.setWidget(richText);
			macroBehaviourView.insert(panel, 0);
			Modeller.setAlertsLine(Modeller.constants.editTheAppearanceThenClickOnSave());
		    };
		};
		firstMenuItem = popupMenu.createMenuItem('R', Modeller.constants.rename(), Modeller.constants.renameTitle(), renameCommand); 
		menu.addItem(firstMenuItem);
	    }
	    Command importCommand = new Command() {

		@Override
		public void execute() {
		    popupMenu.hide();
		    HorizontalPanel importPanel = createImportPanel(macroBehaviourView);
		    macroBehaviourView.insert(importPanel, 1);
		}

	    };
	    menuItem = popupMenu.createMenuItem('A', Modeller.constants.addMicroBehaviour(), Modeller.constants.addMicroBehaviourTitle(), importCommand);
	    if (firstMenuItem == null) {
		firstMenuItem = menuItem;
	    }
	    menu.addItem(menuItem);
	    if (fullPrototype) {
		Command deleteCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			Modeller.instance().removeMacroBehaviour(macroBehaviourView);
			new RemoveMacroBehaviourEvent(macroBehaviourView).addToHistory();
		    }
		};
		menuItem = popupMenu.createMenuItem('D', Modeller.constants.delete(), Modeller.constants.deleteTitle(), deleteCommand); 
		menu.addItem(menuItem);
		if (macroBehaviourView.isActive()) {
		    Command inactivateCommand = new Command() {
			public void execute() {
			    popupMenu.hide();
			    macroBehaviourView.setActive(false);
			    new InactivateMacroBehaviourEvent(macroBehaviourView).addToHistory();
			    macroBehaviourView.macroBehaviourChanged();
			}
		    };
		    menuItem = popupMenu.createMenuItem('I', Modeller.constants.inactivate(), Modeller.constants.inactivateTitle(), inactivateCommand); 
		} else {
		    Command activateCommand = new Command() {
			public void execute() {
			    popupMenu.hide();
			    macroBehaviourView.setActive(true);
			    new ActivateMacroBehaviourEvent(macroBehaviourView).addToHistory();
			    macroBehaviourView.macroBehaviourChanged();
			}
		    };
		    menuItem = popupMenu.createMenuItem('A',
			                                Modeller.constants.activate(),
			                                Modeller.constants.activateTitle(),
			                                activateCommand); 
		}
		menu.addItem(menuItem);
		Command addToPrototypeCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView = 
			    new MacroBehaviourAsMicroBehaviourView(macroBehaviourView);
			macroBehaviourAsMicroBehaviourView.waitForClickOnAMacroBehaviour(true);
		    }
		};
		menuItem = popupMenu.createMenuItem('P',
			                            Modeller.constants.addToAnotherPrototype(),
			                            Modeller.constants.addToAnotherPrototypeTitle(),
			                            addToPrototypeCommand); 
		menu.addItem(menuItem);
		Command addToListCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			MacroBehaviourAsMicroBehaviourView macroBehaviourAsMicroBehaviourView = 
			    new MacroBehaviourAsMicroBehaviourView(macroBehaviourView);
			macroBehaviourAsMicroBehaviourView.waitForClickOnAMacroBehaviour(false);
		    }
		};
		menuItem = popupMenu.createMenuItem(
			'L', Modeller.constants.addToList(), Modeller.constants.addToListTitle(), addToListCommand); 
		menu.addItem(menuItem);
		Command hideDetailsCommand = new Command() {
		    public void execute() {
			popupMenu.hide();
			macroBehaviourView.setShowMicroBehaviours(false);
			macroBehaviourView.setShowHideThisCheckBox(false);
			macroBehaviourView.setShowHowManyInstances(false);
			macroBehaviourView.macroBehaviourChanged();
		    }
		};
		menuItem = popupMenu.createMenuItem('H', Modeller.constants.hideDetails(),  Modeller.constants.hideDetailsTitle(), hideDetailsCommand); 
		menu.addItem(menuItem);
	    }
	} else {
	    Command inspectCommand = new Command() {
		public void execute() {
		    popupMenu.hide();
		    macroBehaviourView.setShowMicroBehaviours(true);
		    macroBehaviourView.setShowHideThisCheckBox(true);
		    macroBehaviourView.setShowHowManyInstances(true);
		    macroBehaviourView.macroBehaviourChanged();
		}
	    };
	    firstMenuItem = popupMenu.createMenuItem('S', Modeller.constants.showDetails(), Modeller.constants.showDetailsTitle(), inspectCommand);
	    menu.addItem(firstMenuItem);
	}
	Command searchReachableMicroBehaviours = new Command() {

	    @Override
	    public void execute() {
		popupMenu.hide();
		Modeller.setAlertsLine(Modeller.constants.searchingPleaseWait());
		// TODO: add filters to the results (e.g. substring match)
		List<SearchResultsItem> searchItems = macroBehaviourView.getReachableMicroBehaviours();
		SearchResultPopup searchResultPopup = new SearchResultPopup(searchItems, Modeller.constants.searchResults());
		searchResultPopup.center();
		searchResultPopup.show();
		Modeller.setAlertsLine("");
	    }
	    
	};
	menuItem = popupMenu.createMenuItem('S', Modeller.constants.searchReachableMicroBehaviours(), Modeller.constants.searchReachableMicroBehavioursTitle(), searchReachableMicroBehaviours);
	menu.addItem(menuItem);
	Command listAttributes = new Command() {

	    @Override
	    public void execute() {
		popupMenu.hide();
		ArrayList<MicroBehaviourView> microBehaviours = macroBehaviourView.getMicroBehavioursRecursively();
		BehaviourComposer.displayAttributes(microBehaviours, getHTML());
	    }
	    
	};
	menuItem = popupMenu.createMenuItem('t', Modeller.constants.listAttributes(), Modeller.constants.listAttributesTitle(), listAttributes);
	menu.addItem(menuItem);
	final int myMacroBehaviourIndex = Modeller.instance().getMacroBehaviourIndex(macroBehaviourView);
	int macroBehaviourCount = Modeller.instance().getMacroBehaviourCount();
	final int nextIndex = myMacroBehaviourIndex+1;
	boolean moveToLeftEdge = nextIndex >= macroBehaviourCount && macroBehaviourCount > 1;
	final int previousIndex = myMacroBehaviourIndex-1;
	boolean moveToRightEdge = previousIndex < 0 && macroBehaviourCount > 1;
	Command moveRight = new Command() {

	    @Override
	    public void execute() {
		popupMenu.hide();
		new SwapPrototypesEvent(myMacroBehaviourIndex, nextIndex).addToHistory();
		Modeller.instance().swapPrototypes(myMacroBehaviourIndex, nextIndex);
	    }
	    
	};
	String movePrototypeRight = moveToLeftEdge ? Modeller.constants.movePrototypeToLeftSide() : 
	                                             Modeller.constants.movePrototypeRight();
	String movePrototypeRightTitle = moveToLeftEdge ? Modeller.constants.movePrototypeToLeftSideTitle() :
	                                                  Modeller.constants.movePrototypeRightTitle();
	MacroBehaviourView macroBehaviourWithNextIndex = Modeller.instance().getMacroBehaviourWithIndex(nextIndex);
	if (macroBehaviourCount <= 1) {
	    movePrototypeRightTitle = ""; // no title if disabled
	} else {
	    movePrototypeRightTitle = moveToLeftEdge ?
		    Modeller.constants.movePrototypeToLeftSideTitle() :
			movePrototypeRightTitle.replace(
				"***prototype name***", 
				CommonUtils.removeHTMLMarkup(macroBehaviourWithNextIndex.getNameHTML()));
	}
	char shortcutKeyRight = moveToLeftEdge ? 'B' : 'g';
	menuItem = popupMenu.createMenuItem(shortcutKeyRight, movePrototypeRight, movePrototypeRightTitle, moveRight);
	menu.addItem(menuItem);
	if (macroBehaviourCount <= 1) {
	    menuItem.setEnabled(false);
	}
	Command moveLeft = new Command() {

	    @Override
	    public void execute() {
		popupMenu.hide();
		new SwapPrototypesEvent(myMacroBehaviourIndex, previousIndex).addToHistory();
		Modeller.instance().swapPrototypes(myMacroBehaviourIndex, previousIndex);	
	    }
	    
	};
	String movePrototypeLeft = moveToRightEdge ? Modeller.constants.movePrototypeToRightSide() : 
                                                     Modeller.constants.movePrototypeLeft();
	String movePrototypeLeftTitle = Modeller.constants.movePrototypeLeftTitle();
	MacroBehaviourView macroBehaviourWithPreviousIndex = Modeller.instance().getMacroBehaviourWithIndex(previousIndex);
	if (macroBehaviourCount <= 1) {
	    movePrototypeLeftTitle = ""; // no title if disabled
	} else {
	    movePrototypeLeftTitle =
		    moveToRightEdge ? Modeller.constants.movePrototypeToRightSideTitle() :
			movePrototypeLeftTitle.replace(
				"***prototype name***", 
				CommonUtils.removeHTMLMarkup(macroBehaviourWithPreviousIndex.getNameHTML()));
	}
	char shortcutKeyLeft = moveToRightEdge ? 'E' : 'f';
	menuItem = popupMenu.createMenuItem(shortcutKeyLeft, movePrototypeLeft, movePrototypeLeftTitle, moveLeft);
	menu.addItem(menuItem);
	if (macroBehaviourCount <= 1) {
	    menuItem.setEnabled(false);
	}
	popupMenu.show();
	Utils.positionPopupMenu(popupClickEvent.getClientX(), popupClickEvent.getClientY(), popupMenu);
    }
    
    protected HorizontalPanel createImportPanel(final MacroBehaviourView macroBehaviourView) {
	final URLEntryBox panel = new URLEntryBox(Modeller.constants.enterAUrlOfAMicroBehaviour());
	Command command = new Command() {
	    
	    @Override
	    public void execute() {
		panel.removeFromParent();
		macroBehaviourView.fetchAndAddMicroBehaviour(panel.getText(), 0);
	    }	    
	};
	panel.addOKCommand(command);
	return panel;
    }
    
    @Override
    public String getHTML() {
	// can't get the HTML from the DOM since they may have been translated
	return currentHTML;
    }
    
    @Override
    public void setHTML(String html) {
	super.setHTML(html);
	currentHTML = html;
    }

}
