# MILP-Struct

This framework enables to compute bounds for structural parameters of graphical representations of ILP or MILP instances.
It parses MILP instances in the MPS file format, in specific the MILP instances from the MIPLIB library http://miplib.zib.de, and then may construct the primal, incidence or dual graph representation of it.
Lower and upper bounds for the structural parameters treewidth, tree-depth and torso-width may then be computed.
 
This framework is based on the libtw library from http://www.treewidth.com, with some adaptations for the computation of the treewidth lower and upper bound approximations. 

This program is published under the GNU Lesser General Public License.