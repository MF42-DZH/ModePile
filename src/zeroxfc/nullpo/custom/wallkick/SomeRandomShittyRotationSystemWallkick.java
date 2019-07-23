package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class SomeRandomShittyRotationSystemWallkick implements Wallkick {
	// So basically, it's a symmetric spiral checker.
	// The further from the edges you are, the more checks it performs.
	
	@Override
	public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward,
			Piece piece, Field field, Controller ctrl) {
		int x2 = 0;
		int y2 = 0;
		int offsetRadius = field.getWidth() + 1;
		int dx = 1;
		int dy = 1;
		int m = 1;  // not sure what this is for...
		int iter = 0;
		
		while (iter < Math.pow(offsetRadius, 2)) {
			while ((2 * x2 * dx) < m) {
				// STH
				if (iter >= Math.pow(offsetRadius, 2)) break;
				
				int x3 = x2;
				x3 *= (rtDir == 1) ? 1 : -1;
				
				if (piece.big)
				{
					x3 *= 2; y2 *= 2;
				}
				
				if ((piece.checkCollision(x + x3, y + y2, rtNew, field) == false)) {
					if (!(x3 == 0 && y2 == 0)) {
						return new WallkickResult(x3, y2, rtNew);
					}
				}
				
				iter++;
				x2 += dx;
			}
			while ((2 * y2 * dy) < m) {
				// STH
				if (iter >= Math.pow(offsetRadius, 2)) break;
				
				int x3 = x2;
				x3 *= (rtDir == 1) ? 1 : -1;
				
				if (piece.big)
				{
					x3 *= 2; y2 *= 2;
				}
				
				if ((piece.checkCollision(x + x3, y + y2, rtNew, field) == false)) {
					if (!(x3 == 0 && y2 == 0)) {
						return new WallkickResult(x3, y2, rtNew);
					}
				}
				
				iter++;
				y2 += dy;
			}
			dx *= -1;
			dy *= -1;
			m++;
		}
		
		/*
		for (int i = 0; i < Math.pow(argX, 2); i++) {
			// Spirals are swine.
			// They are a son of a mother-less goat.
			
			I really have no goddamn clue. 
			 
			if ((-argX/2 < x2) &&
				(x2 <= argX/2) &&
				(-argY/2 < y2) &&
				(y2 <= argY/2)) {
				x3 = x2;
				x3 *= (rtDir == 1) ? -1 : 1;
				
				if (piece.big)
				{
					x3 *= 2; y2 *= 2;
				}
				
				if ((piece.checkCollision(x + x3, y + y2, rtNew, field) == false)) {
					if (!(x3 == 0 && y2 == 0)) {
						return new WallkickResult(x3, y2, rtNew);
					}
				}
			}
			
			if ((x2 == y2) ||
				(x2 < 0 && x2 == -y2) ||
				(x2 > 0 && x2 == (1-y2))) {
				int temp = dx;
				dx = -dy;
				dy = temp;
			}
					
			x2 += dx;
			y2 += dy;
			
		}
		*/
		
		return null;
	}
}
