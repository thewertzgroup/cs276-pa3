#!/usr/bin/env sh
# ./score.sh <ranked_input_file> <relevance_file>
java -Xmx1024m -cp bin/ edu.stanford.cs276.NdcgMain $1 $2
