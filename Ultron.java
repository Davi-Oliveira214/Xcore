package xcore;

import robocode.*;
import java.awt.Color;

public class Ultron extends AdvancedRobot {

    int direcao = 1;

    public void run() {
        // Define as cores: corpo, canhão e radar.
        setColors(Color.gray, Color.red, Color.gray);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        setTurnRadarRight(Double.POSITIVE_INFINITY);

        while (true) {
            setAhead(150 * direcao);
            setTurnRight(45 * direcao);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Mira à frente do inimigo em movimento
        double angulo = getHeading() + e.getBearing()
                      - getGunHeading();

        double ajuste = robocode.util.Utils.normalRelativeAngleDegrees(angulo);
        setTurnGunRight(ajuste);

        // Escolhe a potência do tiro
        if (getGunHeat() == 0) {
            if (e.getDistance() < 150) {
                setFire(3);
            } else if (e.getDistance() < 400) {
                setFire(2);
            } else {
                setFire(1);
            }
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        direcao = -direcao;
        setAhead(200 * direcao);
        setTurnRight(90);
    }

    public void onHitWall(HitWallEvent e) {
        direcao = -direcao;
        setBack(100);
        setTurnRight(90);
    }
}