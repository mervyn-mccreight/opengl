package de.fhwedel.opengl;

import com.hackoeur.jglm.Vec3;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;


public class Input implements KeyListener, MouseListener {
    public static final float SPEED = 0.1f;

    private Vec3 eye = new Vec3(4, 3, 3);
    private Vec3 mouseClickedPos;

    public Input(GLWindow window) {
        window.addKeyListener(this);
        window.addMouseListener(this);
    }

    public Vec3 getEye() {
        return eye;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println(keyEvent.getKeyChar());

        switch (Character.toUpperCase(keyEvent.getKeyChar())) {
            case 'W':
                eye = eye.add(new Vec3(0, 0, -1).multiply(SPEED));
                break;
            case 'A':
                eye = eye.add(new Vec3(-1, 0, 0).multiply(SPEED));
                break;
            case 'S':
                eye = eye.add(new Vec3(0, 0, 1).multiply(SPEED));
                break;
            case 'D':
                eye = eye.add(new Vec3(1, 0, 0).multiply(SPEED));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        mouseClickedPos = new Vec3(mouseEvent.getX(), mouseEvent.getY(), 0);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        Vec3 mousePresedPos = new Vec3(mouseEvent.getX(), 0, mouseEvent.getY());

        if (mouseClickedPos != null) {
            Vec3 diff = mousePresedPos.subtract(mouseClickedPos);
            eye = eye.add(diff);
        }

        mouseClickedPos = mousePresedPos;
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }
}
