package xcore;

import robocode.*;

import java.awt.*;
import java.util.Random;

import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * Xcore - Robô Avançado com Mira Travada e Fuga de Parede Corrigida
 */
public class Xcore extends AdvancedRobot {
    private final Random rand;
    private boolean isStop = false;
    private final double GIRO = 360;

    public Xcore() {
        this.rand = new Random();
    }

    public void run() {
        this.setColors(Color.BLUE, new Color(144, 190, 222), new Color(144, 243, 255));

        //  Separa os movimentos do Corpo, Arma e Radar
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);

        // Faz o radar dar um giro inicial completo de 360° para achar o primeiro inimigo
        this.setTurnRadarRight(360);

        // Loop principal do robô
        while (true) {
            if (!this.isStop)
                this.setAhead(this.rand.nextInt(70) + 80);

            this.rotationEscape();

            this.execute();
        }
    }

    private void rotationEscape() {
        int right = this.rand.nextInt(70);
        int left = 0;

        if (right > 25 && right < 50) {
            this.setTurnRight(right);
            left = this.randomRoad();

            if (left > 85 && left < 125)
                left = 0;
        }

        if (left != 0)
            this.setTurnLeft(left);

        if (getRadarTurnRemaining() == 0)
            this.setTurnRadarRight(GIRO);
    }

    /**
     * onScannedRobot: Ativado sempre que o radar intercepta um inimigo
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Calcula o ângulo absoluto do inimigo na arena
        double anguloAbsolutoInimigo = this.getHeading() + e.getBearing();

        double giroArma = normalRelativeAngleDegrees(anguloAbsolutoInimigo - getGunHeading());
        double giroRadar = normalRelativeAngleDegrees(anguloAbsolutoInimigo - getRadarHeading()) * 2;

        this.setTurnGunRight(giroArma);
        this.setTurnRadarRight(giroRadar);

        if (e.getDistance() < 100)
            this.setFire(3.0);
        else if (e.getDistance() < 200)
            this.setFire(2.5);
        else if (e.getDistance() < 300)
            this.setFire(1.5);
        else
            this.setFire(1);

        if (e.getDistance() < 50)
            this.setAhead(this.randomRoad());
    }

    public void onHitByBullet(HitByBulletEvent e) {
        this.onHit(e.getBearing());
    }

    public void onBulletHit(BulletHitEvent event) {
        this.isStop = true;
    }

    public void onBulletMissed(BulletMissedEvent event) {
        this.isStop = false;
    }

    /**
     * onHitWall: Corrigido o travamento adicionando rotação de fuga sincronizada
     */
    public void onHitWall(HitWallEvent e) {
        this.onHit(e.getBearing());
    }

    public void onHitRobot(HitRobotEvent event) {
        this.onHit(event.getBearing());
    }

    private void onHit(double bearing) {
        this.isStop = false;

        double road = 90;
        double escape = 45;

        if (bearing > -90 && bearing < 90) {
            this.setTurnRight(escape);
            this.setBack(road);
        } else {
            this.setTurnLeft(escape);
            this.setAhead(road);
        }
    }

    private int randomRoad() {
        return this.rand.nextInt(70) + 80;
    }

    public void onWin(WinEvent event) {
        this.setTurnRight(GIRO);
    }
}
