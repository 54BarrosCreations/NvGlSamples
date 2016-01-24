package test;


import com.jogamp.opengl.GLDebugListener;
import com.jogamp.opengl.GLDebugMessage;

public class MyGLDebugListener implements GLDebugListener {
        int recSource;
        int recType;
        int recSeverity;

        String recMsg;
        int recId;
        boolean received = false;

        public MyGLDebugListener(final int recSource, final int recType, final int recSeverity) {
            this.recSource = recSource;
            this.recType = recType;
            this.recSeverity = recSeverity;
            this.recMsg = null;
            this.recId = -1;

        }
        public MyGLDebugListener(final String recMsg, final int recId) {
            this.recSource = -1;
            this.recType = -1;
            this.recSeverity = -1;
            this.recMsg = recMsg;
            this.recId = recId;
        }

        public boolean received() { return received; }

        @Override
        public void messageSent(final GLDebugMessage event) {
            System.err.println("XXX: "+event);
            if(null != recMsg && recMsg.equals(event.getDbgMsg()) && recId == event.getDbgId()) {
                received = true;
            } else if(0 <= recSource && recSource == event.getDbgSource() &&
                                        recType == event.getDbgType() &&
                                        recSeverity== event.getDbgSeverity() ) {
                received = true;
            }
            // Thread.dumpStack();
        }
    }