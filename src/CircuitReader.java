import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.data.Attribute;

public class CircuitReader {
	public static IndexedLineSetFactory ils = new IndexedLineSetFactory();
	public static PointSetFactory ps = new PointSetFactory();

	public static IndexedLineSetFactory readFile(String path)
	{

		BufferedReader br = null;

		try
		{
			br = new BufferedReader(new FileReader(path));

			String line = br.readLine();
			int nrinjections = Integer.parseInt(line);

			line = br.readLine();
			int nredges = Integer.parseInt(line);

			line = br.readLine();
			int nrcoords = Integer.parseInt(line);

			line = br.readLine();// indexes of the injections;
			String[] injid = line.split(",");
			HashSet<Integer> inj = new HashSet<Integer>();
			ps.setVertexCount(nrinjections);
			for (int i = 0; i < nrinjections; i++)
			{
				inj.add(Integer.parseInt(injid[i].trim()));
			}

			ils.setEdgeCount(nredges);
			int[][] idx = new int[nredges][2];
//			HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();
			for (int i = 0; i < nredges; i++)
			{
				line = br.readLine();
				String[] tok = line.split(",");
				for (int j = 0; j < 2; j++)
				{
					int nr = Integer.parseInt(tok[j].trim());
					idx[i][j] = nr - 1;
//					if(count.get(nr) == null)
//						count.put(nr, 1);
//					else
//						count.put(nr, count.get(nr)+1);
				}
			}
			ils.setEdgeIndices(idx);

			List<double[]> points = new ArrayList<double[]>();
			List<double[]> injcoo = new ArrayList<double[]>();
			List<String> injl = new ArrayList<String>();
			List<double[]> injcol = new ArrayList<double[]>();
			ils.setVertexCount(nrcoords);
			for (int i = 0; i < nrcoords; i++)
			{
				line = br.readLine();
				System.out.println(line);
				String[] tok = line.split(",");
				double[] c = new double[4];
				c[3] = 1;
				for (int j = 1; j < 4; j++)
				{
					c[j - 1] = Double.parseDouble(tok[j].trim());
				}
				points.add(c);

				if (inj.contains(Integer.parseInt(tok[0].trim())))
				{
					injcoo.add(c);
				}
			}

			// citeste numele inputurilor
			for (int i = 0; i < inj.size(); i++)
			{
				line = br.readLine();
				;
				if (line == null)
					break;
				String[] injline = line.split(",");

				String injname = URLDecoder.decode(injline[1].trim(), "UTF-8");
				injl.add(injname);

				// inca o iesire prin static - sa modific mai incolo
				Vis.dInputs.add(Integer.parseInt(injline[0].trim()) - 1);
				Vis.dInputs.putName(Integer.parseInt(injline[0].trim()) - 1,
						injname);

				injcol.add(VisProps.grayColor);
			}

			if (points.size() > 0)
				ils.setVertexCoordinates(points.toArray(new double[0][]));
			if (injcoo.size() > 0)
			{
				ps.setVertexCoordinates(injcoo.toArray(new double[0][]));
				ps.setVertexLabels(injl.toArray(new String[0]));
				ps.setVertexColors(injcol.toArray(new double[0][]));
			}

			br.close();

			double[][] edgeColors = new double[nredges][3];
			for (int i = 0; i < idx.length; i++)
			{
				boolean isprimal = true;
				// se poate totusi sa fie doua injection pointuri unul langa
				// celalalt, dar nu mai scriu cod acuma
				for (int j = 0; j < 2; j++)
				{
					if (!inj.contains(idx[i][j] + 1))// +1 pentru ca indicii in
														// fisier sunt de la 1
						isprimal = isprimal
								&& (Math.abs((long) (points.get(idx[i][j])[0])) % 2 == 0);
				}

				if (isprimal)
				{
					edgeColors[i] = VisProps.primalEdgeColor;
				} else
				{
					edgeColors[i] = VisProps.dualEdgeColor;
				}
			}
			ils.setEdgeColors(edgeColors);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		ils.update();
		ps.update();

		return ils;
	}
}
