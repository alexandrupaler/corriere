import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class VerificationReader extends ArrayList<SearchParameters>
{
	private static final long serialVersionUID = 1L;
	
	public String[] getStringArray()
	{
		String[] ret = new String[this.size()];
		int pos=0;
		for(SearchParameters s : this)
			ret[pos++] = s.name;
		return ret;
	}
	
	public SearchParameters getSearchParamForName(String name)
	{
		for(SearchParameters s : this)
			if(s.name.equals(name))
				return s;
		return null;
	}
	
	public VerificationReader(String path, DrawInputs dInputs)
	{
		BufferedReader br = null;

		try
		{
			br = new BufferedReader(new FileReader(path));
			String str = null;
			while((str=br.readLine())!=null)
			{
				SearchParameters param = constructSearchParam(dInputs, str);
				this.add(param);
			}
				
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SearchParameters constructSearchParam(DrawInputs dInputs, String str)
	{
		SearchParameters param = new SearchParameters();
		param.name = str;
		
		String[] xz = str.split(" ");
		for(int i=0; i<xz.length; i++)
		{
			String[] s = xz[i].split(":");
			boolean isx = s[0].toLowerCase().equals("x");
			
			if(s.length == 1)
				continue;
			
			String[] names = s[1].split(",");
			for(String label : names)
			{
				//boolean isprimal = true;
				//if(isprimal)//deocamdata sunt doar in primal
				//{
					String nlabel = label.replace('A', 'Y');//pentru A distillation
					if(isx)
					{
						param.xSearch.add(dInputs.getIdForName(label));
						param.xSearch.add(dInputs.getIdForName(nlabel));
					}
					else
					{
						param.zSearch.add(dInputs.getIdForName(label));
						//param.zSearch.add(dInputs.getIdForName(nlabel));
					}
				//}
			}
		}
		return param;
	}
}
