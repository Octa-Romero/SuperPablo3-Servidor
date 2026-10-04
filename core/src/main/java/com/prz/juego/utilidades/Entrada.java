package com.prz.juego.utilidades;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class Entrada extends InputAdapter {

	private boolean arriba, derecha, izquierda, ataca, escape, f1;

	@Override
	public boolean keyDown(int keycode) {

		if (keycode == Input.Keys.W || keycode == Input.Keys.UP  || keycode == Input.Keys.Z) {
			arriba = true;
		}

		if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
			derecha = true;
		}

		if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
			izquierda = true;
		}

		if (keycode == Input.Keys.X || keycode == Input.Keys.J || keycode == Input.Keys.SPACE) {
			ataca = true;
		}

        if(keycode ==  Input.Keys.ESCAPE) {
            escape = true;
        }

		if (keycode == Input.Keys.F1) {
			f1 = true;
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {

		if (keycode == Input.Keys.W || keycode == Input.Keys.UP || keycode == Input.Keys.Z) {
			arriba = false;
		}

		if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
			derecha = false;
		}

		if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
			izquierda = false;
		}

        if (keycode == Input.Keys.X || keycode == Input.Keys.J || keycode == Input.Keys.SPACE) {
            ataca = false;
        }

        if (keycode == Input.Keys.ESCAPE) {
            escape = false;
        }

        if (keycode == Input.Keys.F1) {
            f1 = false;
        }

		return false;
	}

	public boolean mueveArriba() {
		return arriba;
	}

	public boolean mueveDerecha() {
		return derecha;
	}

	public boolean mueveIzquierda() {
		return izquierda;
	}

	public boolean ataca() {
		if (ataca) {
			ataca = false;
			return true;
		}
		return false;
	}

    public boolean aprietaEscape() {
        if(escape) {
            escape =  false;
            return true;
        }
        return false;
    }

	public boolean aprietaF1() {
		if (f1) {
			f1 = false;
			return true;
		}
		return false;
	}
}
