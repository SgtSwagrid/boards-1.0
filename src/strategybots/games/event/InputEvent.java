package strategybots.games.event;

import org.lwjgl.input.Keyboard;

/**
 * Abstract superclass for events involving input.
 * @author Alec
 */
public abstract class InputEvent extends Event {
    
    /**
     * Abstract superclass for events involving keyboard input.
     */
    public static abstract class KeyboardEvent extends InputEvent {
        
        /** The key which triggered this event. */
        public final int KEY_ID;
        public final String KEY_NAME;
        
        /**
         * Triggers a new KeyboardEvent for the given key value.
         * @param key
         */
        private KeyboardEvent(int key) {
            KEY_ID = key;
            KEY_NAME = Keyboard.getKeyName(key);
            trigger();
        }
        
        @Override
        protected boolean checkCondition(Object c) {
            return (c instanceof String)
                    && c.equals(KEY_NAME);
        }
        
        @Override
        public String toString() { return KEY_NAME; }
        
        /**
         * This event is triggered once whenever a key is pressed.
         */
        public static class KeyPressEvent extends KeyboardEvent {
            public KeyPressEvent(int key) { super(key); }
        }
        
        /**
         * This event is triggered once per tick per key which is held down.
         */
        public static class KeyHoldEvent extends KeyboardEvent {
            public KeyHoldEvent(int key) { super(key); }
        }
        
        /**
         * This event is triggered once whenever a key is released.
         */
        public static class KeyReleaseEvent extends KeyboardEvent {
            public KeyReleaseEvent(int key) { super(key); }
        }
    }
    
    /**
     * Abstract superclass for events involving mouse input.
     */
    public static abstract class MouseEvent extends InputEvent {
        
        /** Represents the position of the cursor (in pixels). (0, 0) is the bottom-left corner. */
        public final int MOUSE_X, MOUSE_Y;
        
        /**
         * Triggers a new MouseEvent at the given screen position.
         * @param mouseX the x position.
         * @param mouseY the y position.
         */
        private MouseEvent(int mouseX, int mouseY) {
            MOUSE_X = mouseX;
            MOUSE_Y = mouseY;
        }
        
        /**
         * Abstract superclass for events involving clicking.
         */
        public static abstract class MouseButtonEvent extends MouseEvent {
            
            /** The button which triggered this event. */
            public final int BUTTON;
            
            /**
             * Triggers a new MouseButtonEvent for the given button value.
             * @param button
             */
            private MouseButtonEvent(int button, int mouseX, int mouseY) {
                super(mouseX, mouseY);
                BUTTON = button;
                trigger();
            }
            
            /**
             * This event is triggered once whenever a button is clicked.
             */
            public static class ClickEvent extends MouseButtonEvent {
                public ClickEvent(int button, int mouseX, int mouseY) {
                    super(button, mouseX, mouseY);
                }
            }
            
            /**
             * This event is triggered once per tick per mouse button which is held down.
             */
            public static class ClickHoldEvent extends MouseButtonEvent {
                public ClickHoldEvent(int button, int mouseX, int mouseY) {
                    super(button, mouseX, mouseY);
                }
            }
            
            /**
             * This event is triggered once whenever a mouse button is released.
             */
            public static class ClickReleaseEvent extends MouseButtonEvent {
                public ClickReleaseEvent(int button, int mouseX, int mouseY) {
                    super(button, mouseX, mouseY);
                }
            }
        }
        
        /**
         * This event is triggered whenever the mouse is moved.
         */
        public static class MouseMoveEvent extends MouseEvent {
            
            /** Represents the change in the cursor position. */
            public final int MOUSE_DX, MOUSE_DY;
            
            /**
             * Triggers a new MouseMoveEvent with the given movement and position.
             * @param mouseDX movement in the x direction.
             * @param mouseDY movement in the y direction.
             * @param mouseX the x position.
             * @param mouseY the y position.
             */
            public MouseMoveEvent(int mouseDX, int mouseDY, int mouseX, int mouseY) {
                super(mouseX, mouseY);
                MOUSE_DX = mouseDX;
                MOUSE_DY = mouseDY;
                trigger();
            }
        }
        
        /**
         * This event is triggered whenever the scroll wheel is used.
         */
        public static class ScrollWheelEvent extends MouseEvent {
            
            /** Represents the change in the scroll wheel orientation. */
            public final int D_WHEEL;
            
            /**
             * Triggers a new ScrollWheelEvent with the given wheel rotation and position.
             * @param dWheel the rotation of the scroll wheel.
             * @param mouseX the x position.
             * @param mouseY the y position.
             */
            public ScrollWheelEvent(int dWheel, int mouseX, int mouseY) {
                super(mouseX, mouseY);
                D_WHEEL = dWheel;
                trigger();
            }
        }
    }
}