// Copyright (C) 2018, Gobierno de España
// This program is licensed and may be used, modified and redistributed under the terms
// of the European Public License (EUPL), either version 1.1 or (at your
// option) any later version as soon as they are approved by the European Commission.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and
// more details.
// You should have received a copy of the EUPL1.1 license
// along with this program; if not, you may find it at
// http://joinup.ec.europa.eu/software/page/eupl/licence-eupl

/** 
 * <b>File:</b><p>es.gob.fire.statistics.ComparatorStatData.java.</p>
 * <b>Description:</b><p>Class that manages </p>
 * <b>Project:</b><p>.</p>
 * <b>Date:</b><p>06/07/2020.</p>
 * @author Spanish Government.
 * @version 1.0, 06/07/2020.
 */
package es.gob.fire.statistics;

import java.util.Comparator;


/** 
 * <p>Class .</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 06/07/2020.
 */
public class ComparatorStatData implements Comparator<StatData>{
   
    /**
     * {@inheritDoc}
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(StatData n1, StatData n2){
   
       
        Long n1Value = n1.getValue();        
        Long n2Value = n2.getValue();
       
        return (n2Value.compareTo(n1Value));
            
    }
   
}