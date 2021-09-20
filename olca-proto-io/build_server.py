import datetime
import shutil
import subprocess


def main():
    print('Build the server application ...')
    subprocess.run([
        'mvn', 'clean', 'package', '-P', 'server-app'],
        shell=True)

    print('Generate the README.pdf ...')
    subprocess.run([
        'pandoc',
        '-o',
        'target\\olca-grpc-server\\README.pdf',
        'server-doc.md'])

    print('Copy the JRE ...')
    shutil.copytree('./jre', './target/olca-grpc-server/jre')

    print('Create the distribution package ...')
    suffix = '_win64_%s' % datetime.date.today()
    shutil.make_archive(
        './target/olca-grpc-server' + suffix,
        'zip',
        './target/olca-grpc-server')

    print('All done!')


if __name__ == '__main__':
    main()
