/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

import uk.ac.lkl.client.event.CompoundEvent;
import uk.ac.lkl.client.event.ModellerEvent;
import uk.ac.lkl.client.event.ReconstructEventsContinutation;
import uk.ac.lkl.client.event.SessionEventsCheckBoxToggledEvent;

/**
 * Implements check box that reconstruct (or undo) a session (except StartEvent and LoadEvent)
 * 
 * 
 * @author Ken Kahn
 *
 */
public class SessionEventsCheckBox extends CheckBox implements ValueChangeHandler<Boolean> {

    // this should be unique among check boxes within the application
    protected String nameId;

    protected String guid;
    protected String doMessage;
    protected String undoMessage;
    protected String extraDoMessage = null;
    protected String extraUndoMessage = null;
    protected BehaviourComposer behaviourComposer;
    protected ReconstructEventsContinutation continuation = null;

    // these are check boxes that should be enabled when this one is ticked
    // and disabled if unticked (unless already ticked)
    protected ArrayList<SessionEventsCheckBox> dependentCheckBoxes = 
            new ArrayList<SessionEventsCheckBox>();

    // to enable undo
    // needed to undo a session 
    private CompoundEvent compoundEvent = null;
    // hack to put it here

    public SessionEventsCheckBox(
            String nameId,
            String label, 
            String guid,
            String doMessage,
            String undoMessage,
            String title,
            // TODO: determine if the following can be eliminated
            // and Modeller.instance() used instead
            BehaviourComposer behaviourComposer) {
        super(label);
        this.nameId = nameId;
        this.guid = guid;
        this.doMessage = doMessage;
        this.undoMessage = undoMessage;
        this.behaviourComposer = behaviourComposer;
        this.addValueChangeHandler(this);
        setTitle(title);
        behaviourComposer.addEnabledCheckBox(this);
    }

    @Override
    public void onValueChange(final ValueChangeEvent<Boolean> event) {
        if (getValue()) { // checked
            behaviourComposer.setInterfaceEnabled(false);
            ModellerEvent.startRecordingCompoundEvent();
            ReconstructEventsContinutation fullContinuation = new ReconstructEventsContinutation() {

                @Override
                public void reconstructSubsequentEvents(ModellerEvent event) {
                    compoundEvent = ModellerEvent.stopRecordingCompoundEvent();
                    behaviourComposer.setInterfaceEnabled(true);
                    communicateDoMessage();
                    checkActionCompleted(true);
                    if (continuation != null) {
                        continuation.reconstructSubsequentEvents(event);
                    }
                }

            };
            behaviourComposer.loadAndReconstructHistory(guid, false, false, Modeller.IGNORE_START_AND_LOAD_EVENTS, true, fullContinuation);
        } else if (compoundEvent == null) {
            // undoing but never did it (well really did it, saved, and loaded model)
            ModellerEvent.startRecordingCompoundEvent();
            ReconstructEventsContinutation fullContinuation = new ReconstructEventsContinutation() {

                @Override
                public void reconstructSubsequentEvents(ModellerEvent modellerEvent) {
                    compoundEvent = ModellerEvent.stopRecordingCompoundEvent();
                    behaviourComposer.setInterfaceEnabled(true);
                    // try again now that we have the compound events to undo
                    undoSession();
                    if (continuation != null) {
                        continuation.reconstructSubsequentEvents(modellerEvent);
                    }
                }

            };
            behaviourComposer.setInterfaceEnabled(false);
            behaviourComposer.loadAndReconstructHistory(
                    guid, false, false, Modeller.IGNORE_START_AND_LOAD_EVENTS_AND_DONT_RECONSTRUCT, true, fullContinuation);
        } else {
            undoSession();
        }
    }

    protected void undoSession() {
        ReconstructEventsContinutation continuation = new ReconstructEventsContinutation() {

            @Override
            public void reconstructSubsequentEvents(ModellerEvent event) {
                communicateUndoMessage();
            }

        };
        compoundEvent.undo(true, false, continuation);
        new SessionEventsCheckBoxToggledEvent(false, this).addToHistory(false);
    }

    public void disableWhenNotChecked(final SessionEventsCheckBox checkBox) {
        addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                tieValueToEnabled(checkBox);
            }

        });
        tieValueToEnabled(checkBox);
        dependentCheckBoxes.add(checkBox);
    }

    protected void tieValueToEnabled(final SessionEventsCheckBox checkBox) {
        if (checkBox.getValue()) {
            // don't disable if ticked
            return;
        }
        Boolean shouldBeEnabled = getValue();
        if (shouldBeEnabled) {
            behaviourComposer.addEnabledCheckBox(checkBox);
        } else {
            behaviourComposer.removeEnabledCheckBox(checkBox);
        }
        if (!shouldBeEnabled) {
            checkBox.setEnabled(false);
        } // else will be made true when EnabledCheckBoxes processed
    }

    public void tieValueToEnabledForAllDependents() {
        for (SessionEventsCheckBox checkBox : dependentCheckBoxes) {
            tieValueToEnabled(checkBox);
        }
    }

    protected void checkActionCompleted(boolean checked) {
        new SessionEventsCheckBoxToggledEvent(true, this).addToHistory(false);
    }

    public String getGuid() {
        return guid;
    }

    protected void communicateDoMessage() {
        if (extraDoMessage != null) {
            Modeller.setAlertsLine(extraDoMessage + " " + doMessage);
            extraDoMessage = null;
        } else {
            Modeller.setAlertsLine(doMessage);
        }
    }

    protected void communicateUndoMessage() {
        if (extraUndoMessage != null) {
            Modeller.setAlertsLine(extraUndoMessage + " " + undoMessage);
            extraUndoMessage = null;
        } else {
            Modeller.setAlertsLine(undoMessage);
        }
    }

    public String getNameId() {
        return nameId;
    }

    public Object getModelXML() {
        // only returns access to check boxes that are checked
        if (getValue()) {
            return "<CheckBox nameId='" + getNameId() + "' guid='" + getGuid() + "'/>";
        } else {
            return "";
        }
    }

    public ReconstructEventsContinutation getContinuation() {
        return continuation;
    }

    public void setContinuation(ReconstructEventsContinutation continuation) {
        this.continuation = continuation;
    }
}
