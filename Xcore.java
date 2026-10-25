package xcore;

import robocode.*;
import robocode.Event;

import java.awt.*;
import java.util.Random;

import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * Xcore - Robô Avançado com Mira Travada e Fuga de Parede Corrigida
 */
public class Xcore extends AdvancedRobot {
    Random rand = new Random();

    public void run() {
        setColors(Color.BLUE, new Color(144, 190, 222), new Color(144, 243, 255));

        //  Separa os movimentos do Corpo, Arma e Radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Faz o radar dar um giro inicial completo de 360° para achar o primeiro inimigo
        setTurnRadarRight(360);

        // Loop principal do robô
        while (true) {
            setAhead(this.rand.nextInt(70) + 80);

            this.rotationEscape();

            execute();
        }
    }

    private void rotationEscape() {
        int right = this.rand.nextInt(70);
        int left = 0;

        if (right > 25 && right < 50) {
            setTurnRight(right);
            left = this.rand.nextInt(70) + 80;

            if (left > 85 && left < 125)
                left = 0;
        }

        if (left != 0)
            setTurnLeft(left);

        if (getRadarTurnRemaining() == 0)
            setTurnRadarRight(360);
    }

    /**
     * onScannedRobot: Ativado sempre que o radar intercepta um inimigo
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Calcula o ângulo absoluto do inimigo na arena
        double anguloAbsolutoInimigo = getHeading() + e.getBearing();

        double giroArma = normalRelativeAngleDegrees(anguloAbsolutoInimigo - getGunHeading());
        double giroRadar = normalRelativeAngleDegrees(anguloAbsolutoInimigo - getRadarHeading()) * 2;

        setTurnGunRight(giroArma);
        setTurnRadarRight(giroRadar);

        if (e.getDistance() < 200) {
            setFire(2.5);
        } else {
            setFire(1.5);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        setBack(this.rand.nextInt(30) + 10);
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
        double road = 90;
        double escape = 45;

        if (bearing > -90 && bearing < 90) {
            setTurnRight(escape);
            setBack(road);
        } else {
            setTurnLeft(escape);
            setAhead(road);
        }
    }

    public void onWin(WinEvent event) {
        setTurnRight(360);
    }
}
