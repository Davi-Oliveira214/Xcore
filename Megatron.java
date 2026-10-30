package xcore;

import robocode.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Megatron extends AdvancedRobot {

    private final Map<String, Double> inimigos = new HashMap<>();
    private String alvo;
    private boolean fugindo = false;
    private long tempoFuga = 0;

    @Override
    public void run() {
        setBodyColor(Color.RED);
        setGunColor(Color.MAGENTA);
        setRadarColor(Color.GREEN);
        setBulletColor(Color.YELLOW);
        setScanColor(Color.CYAN);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            setTurnRadarRight(360);
            execute();

            if (fugindo && getTime() < tempoFuga) {
                setBack(100);
                setTurnRight(45);
            } else {
                fugindo = false;

                if (alvo != null) {
                    setTurnRight(20);
                    setAhead(100);
                } else {
                    setTurnRight(45);
                    setAhead(80);
                }
            }

            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        inimigos.put(e.getName(), e.getDistance());

        if (alvo == null ||
            !inimigos.containsKey(alvo) ||
            e.getDistance() < inimigos.get(alvo)) {
            alvo = e.getName();
        }

        double virarCanhao =
            getHeading() + e.getBearing() - getGunHeading();

        setTurnGunRight(normalizarAngulo(virarCanhao));

        if (e.getDistance() < 150) {
            setFire(3);
        } else if (e.getDistance() < 400) {
            setFire(2);
        } else {
            setFire(1);
        }

        if (!fugindo) {
            double virarCorpo =
                e.getBearing();

            setTurnRight(virarCorpo);
            setAhead(e.getDistance() - 100);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        fugindo = true;
        tempoFuga = getTime() + 20;

        setBack(120);
        setTurnRight(90);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(100);
        setTurnRight(90);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        inimigos.remove(e.getName());

        if (e.getName().equals(alvo)) {
            alvo = null;
        }
    }

    private double normalizarAngulo(double angulo) {
        while (angulo > 180) {
            angulo -= 360;
        }

        while (angulo < -180) {
            angulo += 360;
        }

        return angulo;
    }
}
