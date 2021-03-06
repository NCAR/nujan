#!/usr/bin/env python

import sys, math, re

# Filter a file.

def badparms( msg):
  print 'Error: ' + msg
  print 'Parms:'
  print '  infile'
  print '  outfile'
  sys.exit(1)


def main():
  if len(sys.argv) == 1:
    fin  = sys.stdin
    fout = sys.stdout
  elif len(sys.argv) == 2:
    fin  = open( sys.argv[1], 'r')
    fout = sys.stdout
  elif len(sys.argv) == 3:
    fin  = open( sys.argv[1], 'r')
    fout = open( sys.argv[2], 'w')
  else: badparms('wrong num parms')

  attrs = ['_FillValue']

  lines = fin.readlines()
  fin.close()

###  lines = filterTags( '^ *OFFSET', lines)
  lines = filterAttrs( lines)


  for line in lines:
    print >> fout, line
  fout.close()




###def filterTags( pattern, lines):
###  resLines = []
###  for line : lines:
###    if not re.search( pattern, line): resLines.append( line)
###  return resLines


def filterAttrs( lines):
  resLines = []
  for iline in range( len( lines)):
    ##lines[iline] = lines[iline].rstrip()
    if lines[iline][-1] == '\n': lines[iline] = lines[iline][:-1]

  attrNames = ['_FillValue', '_Unsigned']
  iline = 0
  while iline < len(lines):

    # Match a section of lines like:
    #          ATTRIBUTE "_FillValue" {
    #             DATATYPE  H5T_STD_I16LE
    #             DATASPACE  SIMPLE { ( 1 ) / ( 1 ) }
    #             DATA {
    #             (0): 999
    #             }
    #          }
    # The indentation of the trailing brace is crucial.

    mat = None
    for nm in attrNames:
      pat = '^([ \t]*)ATTRIBUTE "' + nm + '" {$'
      mat = re.match( pat, lines[iline])
      if mat:
        endStg = mat.group(1) + '}'
        foundIt = False
        for jj in range( iline+1, min( iline+20, len(lines))):
          if lines[jj] == endStg:
            foundIt = True
            iline = jj + 1
            break
        if not foundIt: throwerr('end not found for iline: ' + str(iline)
          + '  line: ' + lines[iline])
        break

    if not mat:
      for nm in attrNames:
        pat = '^[ \t]*[a-zA-Z0-9]+:' + nm + ' = '
        mat = re.match( pat, lines[iline])
        if mat:
          iline += 1   # skip the line

    if not mat:
      resLines.append( lines[iline])
      iline += 1
  return resLines


def throwerr( msg):
  raise Exception( msg)




if __name__ == '__main__': main()
